/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.platform.am.utils

import org.exoplatform.platform.am.ex.AddonsManagerException
import org.exoplatform.platform.am.ex.UnknownErrorException

import java.nio.channels.FileChannel
import java.util.Base64

/**
 * Miscellaneous utilities
 */
class FileUtils {

  /**
   * Logger
   */
  private static final Logger LOG = Logger.getInstance()
  
  /**
   * System property prefix for authentication
   */
  private static final String AUTH_PREFIX = "addonsmgr.auth."
  
  /**
   * Authentication types
   */
  private static final String AUTH_TYPE_BASIC = "basic"
  private static final String AUTH_TYPE_BEARER = "bearer"
  
  /**
   * Get authentication type from system properties
   */
  private static String getAuthType() {
    return System.getProperty("${AUTH_PREFIX}type")
  }
  
  /**
   * Get username from system properties
   */
  private static String getAuthUsername() {
    return System.getProperty("${AUTH_PREFIX}username")
  }
  
  /**
   * Get password from system properties
   */
  private static String getAuthPassword() {
    return System.getProperty("${AUTH_PREFIX}password")
  }
  
  /**
   * Get bearer token from system properties
   */
  private static String getAuthToken() {
    return System.getProperty("${AUTH_PREFIX}token")
  }
  
  /**
   * Apply authentication to URL connection if configured
   * @param conn The URL connection to apply authentication to
   */
  private static void applyAuthentication(URLConnection conn) {
    if (!(conn instanceof HttpURLConnection)) {
      return
    }
    
    String authType = getAuthType()
    if (!authType) {
      // No authentication configured
      return
    }
    
    HttpURLConnection httpConn = (HttpURLConnection) conn
    
    switch (authType.toLowerCase()) {
      case AUTH_TYPE_BASIC:
        String username = getAuthUsername()
        String password = getAuthPassword()
        
        if (username && password) {
          String auth = "${username}:${password}"
          String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"))
          httpConn.setRequestProperty("Authorization", "Basic ${encodedAuth}")
          LOG.debug("Applied Basic authentication for user: ${username}")
        } else if (username || password) {
          LOG.warn("Basic authentication configured but username or password missing. Please set -D${AUTH_PREFIX}username and -D${AUTH_PREFIX}password")
        }
        break
        
      case AUTH_TYPE_BEARER:
        String token = getAuthToken()
        
        if (token) {
          httpConn.setRequestProperty("Authorization", "Bearer ${token}")
          LOG.debug("Applied Bearer token authentication")
        } else {
          LOG.warn("Bearer authentication configured but token missing. Please set -D${AUTH_PREFIX}token")
        }
        break
        
      default:
        LOG.warn("Unsupported authentication type: ${authType}. Supported types: ${AUTH_TYPE_BASIC}, ${AUTH_TYPE_BEARER}")
        break
    }
  }

  /**
   * Downloads a file following redirects if required
   * @param message the logging message to display
   * @param url The URL from which to download
   * @param destFile The file to populate
   * @throws IOException If there is an IO error
   */
  static downloadFile(String message, URL url, File destFile) throws IOException {
    downloadFile(message, url.toString(), destFile)
  }

  /**
   * Downloads a file following redirects if required
   * @param message the logging message to display
   * @param url The URL from which to download
   * @param destFile The file to populate
   * @throws AddonsManagerException If there is an error while transferring the file
   */
  static downloadFile(String message, String url, File destFile) throws AddonsManagerException {
    String originalUrl = url
    // Let's do it for each redirection
    while (url) {
      new URL(url).openConnection().with { URLConnection conn ->
        if (conn instanceof HttpURLConnection) {
          conn.instanceFollowRedirects = true
          
          // Apply authentication if configured (optional)
          applyAuthentication(conn)
        }
        url = conn.getHeaderField("Location")
        // No more Location, let's download
        if (!url) {
          if (destFile.exists()) {
            LOG.debug("remoteFile lastModified : ${conn.lastModified}")
            LOG.debug("remoteFile size : ${conn.contentLength}")
            LOG.debug("destFile lastModified : ${destFile.lastModified()}")
            LOG.debug("destFile size : ${destFile.size()}")
          }
          // Same size and date more recent locally, don't touch it.
          if (destFile.exists() && conn.contentLength == destFile.size() && conn.lastModified <= destFile.lastModified()) {
            LOG.withStatusOK("File ${destFile.name} already up-to-date. Skipping download.")
            return
          }
          if (!message) {
            message = "Downloading ${originalUrl} to ${destFile}"
          }
          LOG.withStatus(message) {
            try {
              destFile.withOutputStream { out ->
                conn.inputStream.with { inp ->
                  out << inp
                  inp?.close()
                }
              }
            } catch (FileNotFoundException fnfe) {
              // AM-95 : Don't keep an empty/corrupted downloaded file
              if (destFile.exists()) {
                destFile.delete()
              }
              throw new UnknownErrorException("File not found at URL ${originalUrl}", fnfe)
            } catch (IOException ioe) {
              // AM-95 : Don't keep an empty/corrupted downloaded file
              if (destFile.exists()) {
                destFile.delete()
              }
              throw new UnknownErrorException("I/O error while downloading ${originalUrl}", ioe)
            }
          }
        }
      }
    }
  }

  /**
   * Copy a local file to another location using NIO
   * @param message the logging message to display
   * @param sourceFile the source file to copy
   * @param destFile where the file should be copied
   * @throws IOException If there is an IO error
   */
  static void copyFile(String message, File sourceFile, File destFile) throws IOException {
    copyFile(message, sourceFile, destFile, true)
  }

  /**
   * Copy a local file to another location using NIO
   * @param message the logging message to display
   * @param sourceFile the source file to copy
   * @param destFile where the file should be copied
   * @param warnIfOverride Display a warning if destFile already exists
   * @throws IOException If there is an IO error
   */
  static void copyFile(String message, File sourceFile, File destFile, boolean warnIfOverride) throws IOException {
    if (!destFile.exists()) {
      destFile.createNewFile();
    } else {
      LOG.debug("sourceFile lastModified : ${sourceFile.lastModified()}")
      LOG.debug("sourceFile size : ${sourceFile.size()}")
      LOG.debug("destFile lastModified : ${destFile.lastModified()}")
      LOG.debug("destFile size : ${destFile.size()}")
      // Same size and destFile date more recent, don't touch it.
      if (sourceFile.size() == destFile.size() && sourceFile.lastModified() <= destFile.lastModified()) {
        LOG.withStatusOK("Skipping copy of ${sourceFile.name}. File ${destFile.name} already up-to-date.")
        return
      }
      if (warnIfOverride) {
        LOG.warn("File ${destFile.name} already exists. Replacing it.")
      }
    }
    if (!message) {
      message = "Copying ${sourceFile} to ${destFile}"
    }
    LOG.withStatus(message) {
      FileChannel source = null;
      FileChannel destination = null;

      try {
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
      }
      finally {
        if (source != null) {
          source.close();
        }
        if (destination != null) {
          destination.close();
        }
      }
    }
  }

  /**
   * Create a directory and its required parents.
   * @param dirToCreate The directory to create
   * @throws IOException If an error occurs
   */
  static void mkdirs(File dirToCreate) throws IOException {
    if (!dirToCreate.mkdirs()) {
      throw new IOException("Unable to create directory ${dirToCreate}")
    }
  }

  static String extractFilename(String fullpath) {
    return fullpath.substring(fullpath.lastIndexOf('/') + 1, fullpath.length())
  }

  static String extractDirPath(String fullpath) {
    return fullpath.substring(0, fullpath.lastIndexOf('/'))
  }

  static String extractParentAndFilename(String fullpath) {
    if (fullpath.lastIndexOf("/") < 0) return fullpath
    String subPath = fullpath.substring(0, fullpath.lastIndexOf("/") -1)
    if (subPath.indexOf("/") < 0) {
      return fullpath
    } else {
      return fullpath.substring(subPath.lastIndexOf("/") + 1)
    }
  }

}