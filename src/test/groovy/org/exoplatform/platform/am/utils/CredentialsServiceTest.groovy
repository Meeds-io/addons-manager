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
  
  def "test interpolation with missing env var"() {
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
    cred.token == '${MISSING}'

    cleanup:
    tempFile.delete()
  }
}
