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

import groovy.json.JsonSlurper
import org.exoplatform.platform.am.ex.InvalidJSONException

import java.util.regex.Pattern

/**
 * Service to manage credentials from a JSON file
 */
class CredentialsService {

  private static final Logger LOG = Logger.getInstance()

  private List<Map> credentials = []

  /**
   * Environment variables map (for interpolation)
   */
  Map env = System.getenv()

  CredentialsService() {
    reload()
  }

  /**
   * Reload credentials from the file specified in system property "addonsmgr.auth.credentials.file"
   */
  void reload() {
    String credentialsFilePath = System.getProperty("addonsmgr.auth.credentials.file")
    if (credentialsFilePath) {
      loadCredentials(credentialsFilePath)
    } else {
      credentials = []
    }
  }

  /**
   * Load credentials from a JSON file
   * @param filePath The path to the JSON file
   */
  void loadCredentials(String filePath) {
    File file = new File(filePath)
    if (!file.exists()) {
      LOG.warn("Credentials file not found: ${filePath}")
      return
    }

    try {
      def json = new JsonSlurper().parse(file)
      if (json instanceof Map && json.credentials instanceof List) {
        credentials = json.credentials
      } else if (json instanceof List) {
        credentials = json
      } else {
        throw new InvalidJSONException("Invalid credentials format in ${filePath}. Expected a list or an object with a 'credentials' list.")
      }
      LOG.debug("Loaded ${credentials.size()} credentials from ${filePath}")
    } catch (Exception e) {
      LOG.error("Error loading credentials from ${filePath}: ${e.message}")
    }
  }

  /**
   * Get credentials for a given URL
   * @param url The URL to match
   * @return The matching credentials or null if not found
   */
  Map getCredentialsForURL(String url) {
    for (Map entry in credentials) {
      String pattern = entry.url
      if (pattern && (url == pattern || url.matches(pattern))) {
        return interpolateCredentials(entry)
      }
    }
    return null
  }

  /**
   * Interpolate environment variables in credentials
   * @param entry The credentials entry
   * @return The interpolated credentials
   */
  private Map interpolateCredentials(Map entry) {
    Map result = [:]
    entry.each { k, v ->
      if (v instanceof String) {
        result[k] = interpolate(v)
      } else if (v instanceof Map) {
        result[k] = interpolateCredentials(v)
      } else {
        result[k] = v
      }
    }
    return result
  }

  /**
   * Interpolate environment variables in a string
   * @param value The string to interpolate
   * @return The interpolated string
   */
  private String interpolate(String value) {
    if (!value) return value
    
    // Match ${VAR_NAME}
    return value.replaceAll(/\$\{(.+?)\}/) { match, varName ->
      String envValue = env[varName]
      if (envValue != null) {
        return envValue
      }
      LOG.warn("Environment variable ${varName} not found for interpolation")
      return match
    }
  }
}
