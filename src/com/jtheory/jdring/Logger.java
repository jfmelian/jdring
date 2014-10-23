/*
 *  com/jtheory/jdring/AlarmEntry.java
 *  Copyright (C) 1999 - 2004 jtheory creations, Olivier Dedieu et al.
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.jtheory.jdring;

public class Logger {
    public final static boolean DEBUG = false;

    public static void debug(String alarmName, String message){
        if (DEBUG)
        {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();            
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            System.out.println("[" + Thread.currentThread().getName() + "] " + className + "." + methodName + "():" + lineNumber + " : " + alarmName + " " + message);
        }
    }
    public static void debug(String message)
    {
        if (DEBUG)
        {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();            
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            System.out.println("[" + Thread.currentThread().getName() + "] " + className + "." + methodName + "():" + lineNumber + " : " + message);
        }
    }
}
