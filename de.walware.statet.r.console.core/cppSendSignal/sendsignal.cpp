/*
 * Copyright (C) 2003 Louis Thomas and others.
 * License: http://www.latenighthacking.com/projects/lnhfslicense.html
 */


#ifndef UNICODE
#define UNICODE
#define _UNICODE
#endif

#include "targetver.h"

#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include <aclapi.h>
#include <Dbghelp.h>
//#include <winerror.h>


//####################################################################

typedef unsigned int RETVAL;

#define STRINGIFY(A) #A

#define EXIT_OK 0


#define _HandleError(rv, errorsource) \
    _tprintf(errorsource __T(" failed with 0x%08X.\n"), rv); \
    goto error;

#define _HandleError1(rv, errorsource, str) \
    _tprintf(errorsource __T("(%ls) failed with 0x%08X.\n"), str, rv); \
    goto error;

#define _HandleLastError(rv, errorsource) \
    rv = GetLastError(); \
    _HandleError(rv, errorsource);

#define _TeardownIfError(rv, rv2, errorsource) \
    if (rv2 != EXIT_OK) { \
        _tprintf(errorsource __T(" failed with 0x%08X.\n"), rv2); \
        if (rv == EXIT_OK) { \
            rv = rv2; \
        } \
    }

#define _Verify(expression, rv, label) \
    if (!(expression)) { \
        _tprintf(__T("Verify failed: '%ls' is false.\n"), L#expression); \
        rv = E_UNEXPECTED; \
        goto label; \
    }


//####################################################################

void PrintError(DWORD dwError) {
    LPWSTR szErrorMessage = NULL;
    DWORD dwResult = FormatMessage(
            FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, 
            NULL/*ignored*/, dwError, 0/*language*/, szErrorMessage, 0/*min-size*/,
            NULL/*valist*/);
    if (dwResult == 0) {
        _tprintf(__T("(FormatMessage failed)"));
    } else if (szErrorMessage != NULL) {
        _tprintf(__T("%ls"), szErrorMessage);
    }
    if (szErrorMessage != NULL) {
        LocalFree(szErrorMessage);
    }
}


void PrintHelp(void) {
    _tprintf(
            __T("SendSignal <pid>\n")
            __T("  <pid> - send ctrl-break to process <pid> (hex ok)\n") );
}


RETVAL StartRemoteThread(HANDLE hRemoteProc, LPTHREAD_START_ROUTINE dwEntryPoint){
    RETVAL rv;
    
    // must be cleaned up
    HANDLE hRemoteThread = NULL;
    
    // inject the thread
    hRemoteThread = CreateRemoteThread(hRemoteProc, NULL, 0,
            dwEntryPoint, (void *) CTRL_BREAK_EVENT,
            CREATE_SUSPENDED, NULL);
    if (hRemoteThread == NULL) {
        _HandleLastError(rv, __T("CreateRemoteThread"));
    }
    
    // wake up the thread
    if (ResumeThread(hRemoteThread) == (DWORD) -1) {
        _HandleLastError(rv, __T("ResumeThread"))
    }
    
    // wait for the thread to finish
    if (WaitForSingleObject(hRemoteThread, INFINITE) != WAIT_OBJECT_0) {
        _HandleLastError(rv, __T("WaitForSingleObject"));
    }
    
    // find out what happened
    if (!GetExitCodeThread(hRemoteThread, (LPDWORD) &rv)) {
        _HandleLastError(rv, __T("GetExitCodeThread"));
    }
    
    if (rv == STATUS_CONTROL_C_EXIT) {
        _tprintf(__T("Target process was killed.\n"));
        rv = EXIT_OK;
        goto error;
    }
    if (rv != EXIT_OK) {
        _HandleError(rv, __T("(remote function)"));
        //if (ERROR_INVALID_HANDLE==rv) {
        //    printf("Are you sure this is a console application?\n");
        //}
    }
    
error:
    if (hRemoteThread != NULL) {
        if (!CloseHandle(hRemoteThread)) {
            RETVAL rv2 = GetLastError();
            _TeardownIfError(rv, rv2, __T("CloseHandle"));
        }
    }
    
    return rv;
}


RETVAL SetPrivilege(HANDLE hToken, LPCWSTR szPrivilege, bool bEnablePrivilege) {
    RETVAL rv;
    
    TOKEN_PRIVILEGES tp;
    LUID luid;
    
    if (!LookupPrivilegeValue(NULL, szPrivilege, &luid)) {
        _HandleLastError(rv, __T("LookupPrivilegeValue"));
        goto error;
    }
    
    tp.PrivilegeCount = 1;
    tp.Privileges[0].Luid = luid;
    if (bEnablePrivilege) {
        tp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;
    } else {
        tp.Privileges[0].Attributes = 0;
    }
    
    AdjustTokenPrivileges(hToken, false, &tp, sizeof(TOKEN_PRIVILEGES), NULL, NULL); // may return true though it failed
    if ((rv = GetLastError()) != EXIT_OK) {
        _HandleError(rv, __T("AdjustTokenPrivileges"));
    }
    
error:
    
    return rv;
}

RETVAL AdvancedOpenProcess(DWORD dwPid, HANDLE *phRemoteProc) {
    RETVAL rv, rv2;
    
    #define NEEDEDACCESS    PROCESS_QUERY_INFORMATION | \
            PROCESS_VM_WRITE | PROCESS_VM_READ | PROCESS_VM_OPERATION | PROCESS_CREATE_THREAD
    
    // must be cleaned up
    HANDLE hThisProcToken = NULL;
    
    // initialize out params
    *phRemoteProc = NULL;
    bool bDebugPriv = false;
    
    // get a process handle with the needed access
    *phRemoteProc = OpenProcess(NEEDEDACCESS, false, dwPid);
    if (NULL == *phRemoteProc) {
        rv = GetLastError();
        if (rv != ERROR_ACCESS_DENIED) {
            _HandleError(rv, __T("OpenProcess"));
        }
        _tprintf(__T("Access denied; retrying with increased privileges.\n"));
        
        // give ourselves god-like access over process handles
        if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES, &hThisProcToken)) {
            _HandleLastError(rv, __T("OpenProcessToken"));
        }
        
        rv = SetPrivilege(hThisProcToken, SE_DEBUG_NAME, true);
        if (rv != EXIT_OK) {
            _HandleError1(rv, __T("SetPrivilege"), SE_DEBUG_NAME);
        } else {
            bDebugPriv = true;
        }
        
        // get a process handle with the needed access
        *phRemoteProc = OpenProcess(NEEDEDACCESS, false, dwPid);
        if (*phRemoteProc == NULL) {
            _HandleLastError(rv, __T("OpenProcess"));
        }
    }
    
    // success
    rv = EXIT_OK;
    
error:
    if (rv == ERROR_ACCESS_DENIED && bDebugPriv == false) {
        _tprintf(__T("You need administrative access (debug privilege) to access this process.\n"));
    }
    if (bDebugPriv == true) {
        rv2 = SetPrivilege(hThisProcToken, SE_DEBUG_NAME, false);
        _TeardownIfError(rv, rv2, __T("SetPrivilege"));
    }
    if (hThisProcToken != NULL) {
        if (!CloseHandle(hThisProcToken)) {
            rv2 = GetLastError();
            _TeardownIfError(rv, rv2, __T("CloseHandle"));
        }
    }
    return rv;
}


LPVOID getCtrlRoutine() {
    LPVOID ctrlRoutine;
    
    // CtrlRoutine --> MyHandle --> getCtrlRoutine
    // set the CaptureStackBackTrace's first param to 2 to ingore the MyHandler and getCtrlRoutine calls.
    // should disable complier optimization on Release version.
    USHORT count = CaptureStackBackTrace((ULONG) 2, (ULONG) 1, &ctrlRoutine, NULL);
    if (count != 1) {
        _tprintf(__T("CaptureStackBackTrace error\n"));
        goto error;
    }
    
    HANDLE hProcess = GetCurrentProcess();
    if (!SymInitialize(hProcess, NULL, TRUE)) {
        RETVAL rv; _HandleLastError(rv, __T("SymInitialize"));
    }
    
    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME*sizeof(TCHAR) + sizeof(ULONG64)-1)/sizeof(ULONG64)];
    PSYMBOL_INFO pSymbol = (PSYMBOL_INFO) buffer;
    pSymbol->SizeOfStruct = sizeof(SYMBOL_INFO);
    pSymbol->MaxNameLen = MAX_SYM_NAME;
    
    LPVOID funcCtrlRoutine = NULL;
    DWORD64 dwDisplacement = 0;
    if(!SymFromAddr(hProcess, (DWORD64) ctrlRoutine, &dwDisplacement, pSymbol)) {
        RETVAL rv; _HandleLastError(rv, __T("SymFromAddr"));
    }
    funcCtrlRoutine = reinterpret_cast<LPVOID>(pSymbol->Address);
    
    SymCleanup(hProcess);
    
    return funcCtrlRoutine;
    
error:
    return NULL;
}



//####################################################################

static LPTHREAD_START_ROUTINE g_dwCtrlRoutineAddr = NULL;
static HANDLE g_hAddrFoundEvent = NULL;


BOOL WINAPI MyHandler(DWORD dwCtrlType) {
    // test
    //__asm { int 3 };
    if (dwCtrlType != CTRL_BREAK_EVENT) {
        return FALSE;
    }
    
    //printf("Received ctrl-break event\n");
    if (g_dwCtrlRoutineAddr == NULL) {
        // read the stack base address from the TEB
        g_dwCtrlRoutineAddr = (LPTHREAD_START_ROUTINE) getCtrlRoutine();

        // notify that we now have the address
        if (!SetEvent(g_hAddrFoundEvent)) {
            _tprintf(__T("SetEvent failed with 0x08X.\n"), GetLastError());
        }
    }
    return TRUE;
}


RETVAL GetCtrlRoutineAddress(void) {
    RETVAL rv = EXIT_OK;
    
    // must be cleaned up
    g_hAddrFoundEvent = NULL;
    
    // create an event so we know when the async callback has completed
    g_hAddrFoundEvent = CreateEvent(NULL, TRUE, FALSE, NULL); // no security, manual reset, initially unsignaled, no name
    if (g_hAddrFoundEvent == NULL) {
        _HandleLastError(rv, __T("CreateEvent"));
    }
    
    // request that we be called on system signals
    if (!SetConsoleCtrlHandler(MyHandler, TRUE)) {
        _HandleLastError(rv, __T("SetConsoleCtrlHandler"));
    }
    
    // generate a signal
    if (!GenerateConsoleCtrlEvent(CTRL_BREAK_EVENT, 0)) {
        _HandleLastError(rv, __T("GenerateConsoleCtrlEvent"));
    }
    
    // wait for our handler to be called
    {   DWORD dwWaitResult = WaitForSingleObject(g_hAddrFoundEvent, INFINITE);
        if (dwWaitResult == WAIT_FAILED) {
            _HandleLastError(rv, __T("WaitForSingleObject"));
        }
    }
    
    _Verify(g_dwCtrlRoutineAddr != NULL, rv, error);
    
error:
    if (g_hAddrFoundEvent != NULL) {
        if (!CloseHandle(g_hAddrFoundEvent)) {
            RETVAL rv2 = GetLastError();
            _TeardownIfError(rv, rv2, __T("CloseHandle"));
        }
    }
    
    return rv;
}


int _tmain(int nArgs, TCHAR *argv[]) {
    RETVAL rv;
    
    HANDLE hRemoteProc = NULL;
    HANDLE hRemoteProcToken = NULL;
    bool bSignalThisProcessGroup = false;
    
    if (nArgs != 2
            || ((argv[1][0] == '/' || argv[1][0] == '-')
                    && (argv[1][1] == 'H' || argv[1][1] == 'h'|| argv[1][1] == '?') )) {
        PrintHelp();
        exit(1);
    }
    
    // check for the special parameter
    TCHAR *szPid = argv[1];
    bSignalThisProcessGroup = ('-' == szPid[0]);
    TCHAR *szEnd;
    DWORD dwPid = wcstoul(szPid, &szEnd, 0);
    if (bSignalThisProcessGroup == false && (szPid == szEnd || dwPid == 0)) {
        _tprintf(__T("\"%ls\" is not a valid PID.\n"), szPid);
        rv = ERROR_INVALID_PARAMETER;
        goto error;
    }
    
    //_tprintf(__T("Determining address of kernel32!CtrlRoutine...\n");
    rv = GetCtrlRoutineAddress();
    if (rv != EXIT_OK) {
        _HandleError(rv, __T("GetCtrlRoutineAddress"));
    }
    //_tprintf(__T("Address is 0x%08X.\n", g_dwCtrlRoutineAddr);

    // open the process
    if (argv[1][0] == '-') {
        _tprintf(__T("Sending signal to self...\n"));
        hRemoteProc = GetCurrentProcess();
    } else {
        _tprintf(__T("Sending signal to process %d...\n"), dwPid);
        rv = AdvancedOpenProcess(dwPid, &hRemoteProc);
        if (rv != EXIT_OK) {
            _HandleError1(rv, __T("AdvancedOpenProcess"), argv[1]);
        }
    }
    
    rv = StartRemoteThread(hRemoteProc, g_dwCtrlRoutineAddr);
    if (rv != EXIT_OK) {
        _HandleError(rv, __T("StartRemoteThread"));
    }
    
//done:
    rv = EXIT_OK;
    
error:
    if (hRemoteProc != NULL && hRemoteProc != GetCurrentProcess()) {
        if (!CloseHandle(hRemoteProc)) {
            RETVAL rv2 = GetLastError();
            _TeardownIfError(rv, rv2, __T("CloseHandle"));
        }
    }
    if (rv != EXIT_OK) {
        _tprintf(__T("0x%08X == "), rv);
        PrintError(rv);
    }
    
    return rv;
}
