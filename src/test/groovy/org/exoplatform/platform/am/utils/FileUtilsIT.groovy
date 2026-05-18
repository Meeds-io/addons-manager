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

import org.exoplatform.platform.am.IntegrationTestsSpecification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
@Subject(FileUtils)
class FileUtilsIT extends IntegrationTestsSpecification {

  def setup() {
    // Clear authentication system properties before each test
    System.clearProperty("addonsmgr.auth.type")
    System.clearProperty("addonsmgr.auth.username")
    System.clearProperty("addonsmgr.auth.password")
    System.clearProperty("addonsmgr.auth.token")
  }

  def cleanup() {
    // Clear authentication system properties after each test
    System.clearProperty("addonsmgr.auth.type")
    System.clearProperty("addonsmgr.auth.username")
    System.clearProperty("addonsmgr.auth.password")
    System.clearProperty("addonsmgr.auth.token")
  }

  def "[AM_CAT_01] The download mechanism must follow permanent redirects"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    when:
    FileUtils.downloadFile("Testing a download with permanent redirection (301)","${getWebServerRootUrl()}/catalog-redirect-301.jsp", downloadedFile)
    then:
    originalFile.text.equals(downloadedFile.text)
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_CAT_01] The download mechanism must follow temporary redirects"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    when:
    FileUtils.downloadFile("Testing a download with a temporary redirection (302)","${getWebServerRootUrl()}/catalog-redirect-302.jsp", downloadedFile)
    then:
    originalFile.text.equals(downloadedFile.text)
    cleanup:
    downloadedFile.delete()
  }

  @Unroll
  def "[AM_AUTH_01] Download with #authType authentication should work when credentials are provided"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set authentication properties
    System.setProperty("addonsmgr.auth.type", authType)
    if (authType == "basic") {
      System.setProperty("addonsmgr.auth.username", username)
      System.setProperty("addonsmgr.auth.password", password)
    } else if (authType == "bearer") {
      System.setProperty("addonsmgr.auth.token", token)
    }
    
    when:
    FileUtils.downloadFile("Testing download with ${authType} auth", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)
    
    then:
    originalFile.text.equals(downloadedFile.text)
    
    cleanup:
    downloadedFile.delete()
    
    where:
    authType | username | password | token
    "basic"  | "testuser" | "testpass" | null
    "bearer" | null      | null     | "test-token-123"
  }

  def "[AM_AUTH_02] Download should fail when basic authentication is configured but credentials are missing"() {
    setup:
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set only type, missing username and password
    System.setProperty("addonsmgr.auth.type", "basic")
    
    when:
    FileUtils.downloadFile("Testing download with incomplete basic auth", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)
    
    then:
    thrown(Exception)
    
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_AUTH_03] Download should fail when bearer authentication is configured but token is missing"() {
    setup:
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set only type, missing token
    System.setProperty("addonsmgr.auth.type", "bearer")
    
    when:
    FileUtils.downloadFile("Testing download with incomplete bearer auth", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)
    
    then:
    thrown(Exception)
    
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_AUTH_04] Download should work without authentication when no auth properties are set"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    
    // No authentication properties set
    
    when:
    FileUtils.downloadFile("Testing download without authentication", 
                          "${getWebServerRootUrl()}/catalog.json", 
                          downloadedFile)
    
    then:
    originalFile.text.equals(downloadedFile.text)
    
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_AUTH_05] Download should work with unsupported auth type (should fallback to no auth)"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set unsupported authentication type
    System.setProperty("addonsmgr.auth.type", "oauth2")
    System.setProperty("addonsmgr.auth.username", "testuser")
    System.setProperty("addonsmgr.auth.password", "testpass")
    
    when:
    FileUtils.downloadFile("Testing download with unsupported auth type", 
                          "${getWebServerRootUrl()}/catalog.json", 
                          downloadedFile)
    
    then:
    originalFile.text.equals(downloadedFile.text)
    
    cleanup:
    downloadedFile.delete()
  }

  @Unroll
  def "[AM_AUTH_06] Download should work with #authType authentication when redirects are involved"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set authentication properties
    System.setProperty("addonsmgr.auth.type", authType)
    if (authType == "basic") {
      System.setProperty("addonsmgr.auth.username", username)
      System.setProperty("addonsmgr.auth.password", password)
    } else if (authType == "bearer") {
      System.setProperty("addonsmgr.auth.token", token)
    }
    
    when:
    FileUtils.downloadFile("Testing download with ${authType} auth and redirect", 
                          "${getWebServerRootUrl()}/protected-catalog-redirect-301.jsp", 
                          downloadedFile)
    
    then:
    originalFile.text.equals(downloadedFile.text)
    
    cleanup:
    downloadedFile.delete()
    
    where:
    authType | username | password | token
    "basic"  | "testuser" | "testpass" | null
    "bearer" | null      | null     | "test-token-123"
  }

  def "[AM_AUTH_07] Download should handle case-insensitive authentication type"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set authentication type in uppercase
    System.setProperty("addonsmgr.auth.type", "BASIC")
    System.setProperty("addonsmgr.auth.username", "testuser")
    System.setProperty("addonsmgr.auth.password", "testpass")
    
    when:
    FileUtils.downloadFile("Testing download with uppercase auth type", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)
    
    then:
    originalFile.text.equals(downloadedFile.text)
    
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_AUTH_08] Download should handle authentication with special characters in credentials"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    
    // Set authentication with special characters - escape the dollar sign
    System.setProperty("addonsmgr.auth.type", "basic")
    System.setProperty("addonsmgr.auth.username", "user@domain.com")
    System.setProperty("addonsmgr.auth.password", "p@ssw0rd!@#\$%")
    
    when:
    FileUtils.downloadFile("Testing download with special characters in credentials", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)
    
    then:
    originalFile.text.equals(downloadedFile.text)
    
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_AUTH_09] Download should not apply authentication for non-HTTP URLs"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")

    // Set authentication properties
    System.setProperty("addonsmgr.auth.type", "basic")
    System.setProperty("addonsmgr.auth.username", "testuser")
    System.setProperty("addonsmgr.auth.password", "testpass")

    when:
    // This should work even with auth properties set because it's a local file
    FileUtils.copyFile("Testing copy without auth", originalFile, downloadedFile)

    then:
    originalFile.text.equals(downloadedFile.text)

    cleanup:
    downloadedFile.delete()
  }

  def "[AM_AUTH_10] Download with credentials from JSON file should work"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")

    // Create credentials file
    File credFile = File.createTempFile("credentials", ".json")
    credFile.text = """
    [
      {
        "url": "${getWebServerRootUrl()}/protected-catalog.json",
        "type": "basic",
        "username": "testuser",
        "password": "testpass"
      }
    ]
    """
    System.setProperty("addonsmgr.auth.credentials.file", credFile.absolutePath)
    FileUtils.CREDENTIALS_SERVICE.reload()

    when:
    FileUtils.downloadFile("Testing download with JSON credentials", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)

    then:
    originalFile.text.equals(downloadedFile.text)

    cleanup:
    downloadedFile.delete()
    credFile.delete()
    System.clearProperty("addonsmgr.auth.credentials.file")
    FileUtils.CREDENTIALS_SERVICE.reload()
  }

  def "[AM_AUTH_11] Download with credentials from JSON file and env var interpolation should work"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")

    // Create credentials file
    File credFile = File.createTempFile("credentials", ".json")
    credFile.text = """
    [
      {
        "url": "${getWebServerRootUrl()}/protected-catalog.json",
        "type": "basic",
        "username": "testuser",
        "password": "\${TEST_PASS_VAR}"
      }
    ]
    """
    System.setProperty("addonsmgr.auth.credentials.file", credFile.absolutePath)
    FileUtils.CREDENTIALS_SERVICE.env = [TEST_PASS_VAR: "testpass"]
    FileUtils.CREDENTIALS_SERVICE.reload()

    when:
    FileUtils.downloadFile("Testing download with JSON credentials and interpolation", 
                          "${getWebServerRootUrl()}/protected-catalog.json", 
                          downloadedFile)

    then:
    originalFile.text.equals(downloadedFile.text)

    cleanup:
    downloadedFile.delete()
    credFile.delete()
    System.clearProperty("addonsmgr.auth.credentials.file")
    FileUtils.CREDENTIALS_SERVICE.env = System.getenv()
    FileUtils.CREDENTIALS_SERVICE.reload()
  }
  }