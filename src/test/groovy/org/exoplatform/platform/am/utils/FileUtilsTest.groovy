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

import com.sun.net.httpserver.HttpServer
import org.exoplatform.platform.am.UnitTestsSpecification

/**
 * Checks the Authorization header that FileUtils.downloadFile sends, against an in-process HTTP server.
 */
class FileUtilsTest extends UnitTestsSpecification {

  private static final List<String> AUTH_PROPERTIES = ["type", "username", "password", "token"].collect { "addonsmgr.auth." + it }

  HttpServer server
  /** Authorization header received for each request path (null when none was sent) */
  Map<String, String> received = [:]
  File credentialsFile

  def setup() {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0)
    server.createContext("/") { exchange ->
      received[exchange.requestURI.path] = exchange.requestHeaders.getFirst("Authorization")
      byte[] body = "content".bytes
      exchange.sendResponseHeaders(200, body.length)
      exchange.responseBody.withCloseable { it.write(body) }
    }
    server.start()
    AUTH_PROPERTIES.each { System.clearProperty(it) }
    System.clearProperty("addonsmgr.auth.credentials.file")
    FileUtils.CREDENTIALS_SERVICE.env = [TEST_PASS: 'p$1\\w']
    FileUtils.CREDENTIALS_SERVICE.reload()
  }

  def cleanup() {
    server.stop(0)
    AUTH_PROPERTIES.each { System.clearProperty(it) }
    System.clearProperty("addonsmgr.auth.credentials.file")
    FileUtils.CREDENTIALS_SERVICE.env = System.getenv()
    FileUtils.CREDENTIALS_SERVICE.reload()
    credentialsFile?.delete()
  }

  private String getRoot() {
    "http://127.0.0.1:${server.address.port}"
  }

  private void useCredentialsFile(String json) {
    credentialsFile = File.createTempFile("credentials", ".json")
    credentialsFile.text = json.replace("ROOT", root.replace(".", "\\\\."))
    System.setProperty("addonsmgr.auth.credentials.file", credentialsFile.absolutePath)
    FileUtils.CREDENTIALS_SERVICE.reload()
  }

  private void download(String path) {
    File dest = File.createTempFile("download", ".txt")
    try {
      FileUtils.downloadFile("test", root + path, dest)
    } finally {
      dest.delete()
    }
  }

  private static String basic(String user, String pass) {
    "Basic " + Base64.encoder.encodeToString("${user}:${pass}".getBytes("UTF-8"))
  }

  def "a matching file entry is used instead of the addonsmgr.auth properties"() {
    given:
    System.setProperty("addonsmgr.auth.type", "bearer")
    System.setProperty("addonsmgr.auth.token", "from-properties")
    useCredentialsFile('[{ "url": "ROOT/a/.*", "type": "basic", "username": "u1", "password": "${TEST_PASS}" }]')

    when:
    download("/a/file.zip")

    then:
    received["/a/file.zip"] == basic("u1", 'p$1\\w')
  }

  def "a bearer file entry sends its token"() {
    given:
    useCredentialsFile('[{ "url": "ROOT/.*", "type": "bearer", "token": "tok" }]')

    when:
    download("/b")

    then:
    received["/b"] == "Bearer tok"
  }

  def "the addonsmgr.auth properties are the fallback when no entry matches"() {
    given:
    System.setProperty("addonsmgr.auth.type", "bearer")
    System.setProperty("addonsmgr.auth.token", "from-properties")
    useCredentialsFile('[{ "url": "ROOT/a/.*", "type": "bearer", "token": "entry" }]')

    when:
    download("/other")

    then:
    received["/other"] == "Bearer from-properties"
  }

  def "nothing is sent when nothing is configured"() {
    when:
    download("/none")

    then:
    received.containsKey("/none")
    received["/none"] == null
  }

  def "an entry with no type is skipped and the properties apply"() {
    given:
    System.setProperty("addonsmgr.auth.type", "bearer")
    System.setProperty("addonsmgr.auth.token", "from-properties")
    useCredentialsFile('[{ "url": "ROOT/.*", "username": "u", "password": "p" }]')

    when:
    download("/notype")

    then:
    received["/notype"] == "Bearer from-properties"
  }

  def "an entry that is only found inside the url, not matched as a whole, sends nothing"() {
    given:
    useCredentialsFile('[{ "url": "ROOT/ok/.*", "type": "bearer", "token": "tok" }]')

    when:
    download("/evil?u=${root}/ok/x")

    then:
    received["/evil"] == null
  }

  def "a matched entry whose variable is not set sends nothing, not its placeholder nor the properties"() {
    given:
    System.setProperty("addonsmgr.auth.type", "bearer")
    System.setProperty("addonsmgr.auth.token", "from-properties")
    useCredentialsFile('[{ "url": "ROOT/.*", "type": "bearer", "token": "${UNSET_TOKEN}" }]')

    when:
    download("/unset")

    then:
    received.containsKey("/unset")
    received["/unset"] == null
  }
}
