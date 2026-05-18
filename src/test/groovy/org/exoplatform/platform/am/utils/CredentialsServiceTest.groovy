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

import org.exoplatform.platform.am.UnitTestsSpecification

class CredentialsServiceTest extends UnitTestsSpecification {

  def "test load credentials from JSON file (object format)"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '''
    {
      "credentials": [
        {
          "url": "https://server1.com/.*",
          "type": "basic",
          "username": "user1",
          "password": "pass1"
        },
        {
          "url": "https://server2.com/.*",
          "type": "bearer",
          "token": "${TOKEN_VAR}"
        }
      ]
    }
    '''
    CredentialsService service = new CredentialsService()
    service.env = [TOKEN_VAR: "secret-token"]

    when:
    service.loadCredentials(tempFile.absolutePath)

    then:
    Map cred1 = service.getCredentialsForURL("https://server1.com/api")
    cred1.type == "basic"
    cred1.username == "user1"
    cred1.password == "pass1"

    Map cred2 = service.getCredentialsForURL("https://server2.com/download")
    cred2.type == "bearer"
    cred2.token == "secret-token"

    cleanup:
    tempFile.delete()
  }

  def "test load credentials from JSON file (list format)"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '''
    [
      {
        "url": "https://server1.com/.*",
        "type": "basic",
        "username": "user1",
        "password": "pass1"
      }
    ]
    '''
    CredentialsService service = new CredentialsService()

    when:
    service.loadCredentials(tempFile.absolutePath)

    then:
    Map cred1 = service.getCredentialsForURL("https://server1.com/api")
    cred1.type == "basic"
    cred1.username == "user1"
    cred1.password == "pass1"

    cleanup:
    tempFile.delete()
  }

  def "test regex matching"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '''
    [
      {
        "url": "https://.*\\\\.exoplatform\\\\.org/.*",
        "type": "basic",
        "username": "exo",
        "password": "exo"
      }
    ]
    '''
    CredentialsService service = new CredentialsService()
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://repository.exoplatform.org/public")?.username == "exo"
    service.getCredentialsForURL("https://meeds.io") == null

    cleanup:
    tempFile.delete()
  }
  
  def "test an unset variable makes the matched entry unusable instead of sending its placeholder"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '''
    [
      {
        "url": "https://server.com",
        "type": "bearer",
        "token": "${MISSING}"
      }
    ]
    '''
    CredentialsService service = new CredentialsService()
    service.env = [:]
    service.loadCredentials(tempFile.absolutePath)

    when:
    Map cred = service.getCredentialsForURL("https://server.com")

    then:
    cred.unusable
    !cred.token

    cleanup:
    tempFile.delete()
  }

  def "test invalid regex in credentials file does not break matching"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '''
    [
      { "url": "https://[broken", "type": "bearer", "token": "x" },
      { "url": "https://ok.com/.*", "type": "bearer", "token": "y" }
    ]
    '''
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    CredentialsService service = new CredentialsService()
    service.log = new Logger(new Console(new ByteArrayInputStream(new byte[0]), out, out))
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://ok.com/a")?.token == "y"
    service.getCredentialsForURL("https://other.com") == null
    // dropped once at load, with its position and reason, not on every lookup
    out.toString().count("Ignored entry #1") == 1
    out.toString().contains("invalid url pattern")
    !out.toString().contains("Invalid URL pattern in credentials file")

    cleanup:
    tempFile.delete()
  }

  def "test reload after a failed load drops previous credentials"() {
    given:
    File good = File.createTempFile("credentials", ".json")
    good.text = '[{ "url": "https://ok.com/.*", "type": "bearer", "token": "y" }]'
    File bad = File.createTempFile("credentials", ".json")
    bad.text = 'not json'
    CredentialsService service = new CredentialsService()
    service.loadCredentials(good.absolutePath)

    when:
    service.loadCredentials(bad.absolutePath)

    then:
    service.getCredentialsForURL("https://ok.com/a") == null

    cleanup:
    good.delete()
    bad.delete()
  }

  def "test interpolated values keep dollar signs and backslashes"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '[{ "url": "https://server.com/.*", "type": "basic", "username": "u", "password": "${PASS}" }]'
    CredentialsService service = new CredentialsService()
    service.env = [PASS: 'p$1\\w$$d']
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://server.com/a").password == 'p$1\\w$$d'

    cleanup:
    tempFile.delete()
  }

  def "test entries that are not JSON objects are ignored"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '["oops", 3, null, { "url": "https://ok.com/.*", "type": "bearer", "token": "y" }]'
    CredentialsService service = new CredentialsService()
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://ok.com/a")?.token == "y"
    service.getCredentialsForURL("https://other.com/a") == null

    cleanup:
    tempFile.delete()
  }

  def "test a malformed file never logs its content"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    // missing comma after the username: the parser stops on the next line, which holds the password,
    // and its message quotes that line
    tempFile.text = '''[
  {
    "url": "https://server.com/.*",
    "type": "basic",
    "username": "u"
    "password": "S3cr3tPass!",
    "other": "x"
  }
]'''
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    CredentialsService service = new CredentialsService()
    service.log = new Logger(new Console(new ByteArrayInputStream(new byte[0]), out, out))

    when:
    service.loadCredentials(tempFile.absolutePath)

    then:
    service.getCredentialsForURL("https://server.com/a") == null
    String logged = out.toString()
    logged.contains("Invalid JSON in credentials file")
    logged.contains("near line 6")
    !logged.contains("S3cr3tPass")

    cleanup:
    tempFile.delete()
  }

  def "test entries that cannot authenticate are skipped at load so the next one applies"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '''
    [
      { "url": "https://s.com/.*", "username": "u", "password": "p" },
      { "url": "https://s.com/.*", "type": "digest", "username": "u", "password": "p" },
      { "url": "https://s.com/.*", "type": "basic", "username": "u" },
      { "url": "https://s.com/.*", "type": "bearer" },
      { "type": "bearer", "token": "t" },
      { "url": "https://s.com/.*", "type": "BEARER", "token": "good" }
    ]
    '''
    CredentialsService service = new CredentialsService()
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://s.com/a").token == "good"

    cleanup:
    tempFile.delete()
  }

  def "test the url is a pattern: a variable in it is not interpolated and does not match"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '[{ "url": "https://${HOST}/.*", "type": "bearer", "token": "t" }]'
    CredentialsService service = new CredentialsService()
    service.env = [HOST: "nexus.example.com"]
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://nexus.example.com/a") == null

    cleanup:
    tempFile.delete()
  }

  def "test the url must match as a whole, not be found inside another url"() {
    given:
    File tempFile = File.createTempFile("credentials", ".json")
    tempFile.text = '[{ "url": "https://ok\\\\.com/.*", "type": "bearer", "token": "t" }]'
    CredentialsService service = new CredentialsService()
    service.loadCredentials(tempFile.absolutePath)

    expect:
    service.getCredentialsForURL("https://ok.com/x")?.token == "t"
    service.getCredentialsForURL("https://evil.net/?u=https://ok.com/x") == null
    service.getCredentialsForURL("https://ok-com/x") == null

    cleanup:
    tempFile.delete()
  }
}
