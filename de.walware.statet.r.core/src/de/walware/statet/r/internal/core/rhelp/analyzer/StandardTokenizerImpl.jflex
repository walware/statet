/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.walware.statet.r.internal.core.rhelp.analyzer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

%%

%class StandardTokenizerImpl
%unicode
%integer
%function getNextToken
%pack
%char

%{

public static final int ALPHANUM          = StandardTokenizer.ALPHANUM;
public static final int APOSTROPHE        = StandardTokenizer.APOSTROPHE;
public static final int ACRONYM           = StandardTokenizer.ACRONYM;
public static final int COMPANY           = StandardTokenizer.COMPANY;
public static final int EMAIL             = StandardTokenizer.EMAIL;
public static final int HOST              = StandardTokenizer.HOST;
public static final int NUM               = StandardTokenizer.NUM;
public static final int CJ                = StandardTokenizer.CJ;

public static final String [] TOKEN_TYPES = StandardTokenizer.TOKEN_TYPES;

public final int yychar()
{
    return yychar;
}

/**
 * Resets the Tokenizer to a new Reader.
 */
final void reset(java.io.Reader r) {
  // reset to default buffer size, if buffer has grown
  if (zzBuffer.length > ZZ_BUFFERSIZE) {
    zzBuffer = new char[ZZ_BUFFERSIZE];
  }
  yyreset(r);
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(Token t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

/**
 * Fills TermAttribute with the current token text.
 */
final void getText(TermAttribute t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

%}

// basic word: a sequence of digits & letters
ALPHANUM   = ({LETTER}|[:digit:])+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+

// acronyms: U.S.A., I.B.M., etc.
// use a post-filter to remove dots
ACRONYM    =  {LETTER} "." ({LETTER} ".")+

// company names like AT&T and Excite@Home.
COMPANY    =  {ALPHA} ("&"|"@") {ALPHA}

// email addresses
EMAIL      =  {ALPHANUM} (("."|"-"|"_") {ALPHANUM})* "@" {ALPHANUM} (("."|"-") {ALPHANUM})+

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ([:digit:] ({ALPHANUM} {P})+
           | [:digit:] ({ALPHANUM} {P} {ALPHANUM})+)

// punctuation
P	         = ("_"|"-"|"/"|"."|",")

// at least one digit
//HAS_DIGIT  = ({LETTER}|[:digit:])* [:digit:] ({LETTER}|[:digit:])*

ALPHA      = ({LETTER})+

LETTER     = !(![:letter:]|![\u0000-\u03ff])

// Chinese and Japanese (but NOT Korean, which is included in [:letter:])
CJ         = !(![:letter:]|![\u0400-\uffff])

WHITESPACE = \r\n | [ \r\n\t\f]

%%

{ALPHANUM}                                                     { return ALPHANUM; }
{APOSTROPHE}                                                   { return APOSTROPHE; }
{ACRONYM}                                                      { return ACRONYM; }
{COMPANY}                                                      { return COMPANY; }
{EMAIL}                                                        { return EMAIL; }
{NUM}                                                          { return NUM; }
{CJ}                                                           { return CJ; }

/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
