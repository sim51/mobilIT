/**
 * This file is part of MobilIT.
 *
 * MobilIT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobilIT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MobilIT. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @See https://github.com/sim51/mobilIT
 */
package fr.mobilit.neo4j.server.exception;

/**
 * MobilIT Exception class.
 * 
 * @author bsimard
 * 
 */
public class MobilITException extends Exception {

    /**
     * Construct a <code>MobilITException</code> with the specified detail message.
     * 
     * @param msg the detail message
     */
    public MobilITException(String message) {
        super(message);
    }

    /**
     * Construct a <code>MobilITException</code> with the specified detail message and nested exception.
     * 
     * @param msg the detail message
     * @param cause the nested exception
     */
    public MobilITException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a <code>MobilITException</code> with the nested exception.
     * 
     * @param cause the nested exception
     */
    public MobilITException(Throwable cause) {
        super(cause);
    }
}
