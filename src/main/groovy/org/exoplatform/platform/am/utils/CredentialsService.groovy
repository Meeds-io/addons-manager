/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2026 Meeds Association
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

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Service to manage credentials from a JSON file
 */
class CredentialsService {

  private static final List<String> SECRET_FIELDS = ["username", "password", "token"]

  private static final String TYPE_BASIC = "basic"

  private static final String TYPE_BEARER = "bearer"

  /**
   * Logger (injectable for tests)
   */
  Logger log = Logger.getInstance()

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
   * Load credentials from a JSON file. An entry that is not a JSON object, or that cannot authenticate
   * (no url, unsupported or missing type, missing username, password or token), is skipped with a warning.
   * @param filePath The path to the JSON file
   */
  void loadCredentials(String filePath) {
    credentials = []
    File file = new File(filePath)
    if (!file.exists()) {
      log.warn("Credentials file not found: ${filePath}")
      return
    }

    def json
    try {
      json = new JsonSlurper().parse(file)
    } catch (Exception e) {
      // The message of a parse error quotes the offending line of the file, which may hold a secret
      java.util.regex.Matcher line = (e.message =~ /line number (\d+)/)
      log.error("Invalid JSON in credentials file ${filePath} (${e.getClass().simpleName}${line.find() ? ' near line ' + line.group(1) : ''})")
      return
    }

    List entries = null
    if (json instanceof Map && json.credentials instanceof List) {
      entries = json.credentials
    } else if (json instanceof List) {
      entries = json
    }
    if (entries == null) {
      log.error("Invalid credentials format in ${filePath}. Expected a list or an object with a 'credentials' list.")
      return
    }
    credentials = []
    entries.eachWithIndex { entry, int i ->
      String problem = entry instanceof Map ? validate(entry) : "not a JSON object"
      if (problem) {
        log.warn("Ignored entry #${i + 1} of ${filePath}: ${problem}")
      } else {
        credentials << entry
      }
    }
    log.debug("Loaded ${credentials.size()} credentials from ${filePath}")
  }

  /**
   * Get credentials for a given URL
   * @param url The URL to match
   * @return The matching credentials, null if no entry matches, or a map with <code>unusable: true</code>
   *         when the entry matches but a variable it references is not set (nothing must be sent then)
   */
  Map getCredentialsForURL(String url) {
    for (Map entry in credentials) {
      if (matches(url, entry.url as String)) {
        return interpolateCredentials(entry)
      }
    }
    return null
  }

  private static String validate(Map entry) {
    if (!entry.url) {
      return "no url"
    }
    try {
      Pattern.compile(entry.url as String)
    } catch (PatternSyntaxException e) {
      return "invalid url pattern"
    }
    String type = entry.type
    if (!type) {
      return "no type"
    }
    switch (type.toLowerCase()) {
      case TYPE_BASIC:
        return entry.username && entry.password ? null : "basic type needs a username and a password"
      case TYPE_BEARER:
        return entry.token ? null : "bearer type needs a token"
      default:
        return "unsupported type '${type}'"
    }
  }

  private static boolean matches(String url, String pattern) {
    return url == pattern || url.matches(pattern)
  }

  /**
   * Interpolate environment variables in the username, password and token of an entry
   * (the url is a pattern and is never interpolated)
   */
  private Map interpolateCredentials(Map entry) {
    Map result = new LinkedHashMap(entry)
    List<String> missing = []
    SECRET_FIELDS.each { String field ->
      if (entry[field] instanceof String) {
        result[field] = interpolate(entry[field] as String, missing)
      }
    }
    if (missing) {
      log.warn("Environment variable ${missing.unique().join(', ')} not found for the credentials of ${entry.url}: no authentication sent")
      return [unusable: true]
    }
    return result
  }

  private String interpolate(String value, List<String> missing) {
    // Match ${VAR_NAME}
    return value.replaceAll(/\$\{(.+?)\}/) { match, varName ->
      String envValue = env[varName]
      if (envValue == null) {
        missing << varName
        return match
      }
      return envValue
    }
  }
}
