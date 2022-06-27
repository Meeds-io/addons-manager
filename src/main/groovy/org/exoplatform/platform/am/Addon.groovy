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
package org.exoplatform.platform.am

import groovy.transform.Canonical
import org.eclipse.aether.util.version.GenericVersionScheme
import org.exoplatform.platform.am.settings.PlatformSettings

@Canonical(includes = ['id', 'version'])
class Addon implements Comparable<Addon> {

  private static final GenericVersionScheme VERSION_SCHEME = new GenericVersionScheme()

  String id
  String version
  Boolean unstable
  String name
  String description
  String releaseDate
  String sourceUrl
  String screenshotUrl
  String thumbnailUrl
  String documentationUrl
  String downloadUrl
  String vendor
  String author
  String authorEmail
  String license
  String licenseUrl
  Boolean supported
  Boolean mustAcceptLicense
  List<String> supportedDistributions
  List<PlatformSettings.AppServerType> supportedApplicationServers
  String compatibility
  List<String> installedLibraries
  List<String> installedWebapps
  List<String> installedProperties
  List<String> installedOthersFiles
  List<String> overwrittenFiles

  boolean isSnapshot() {
    return version ==~ /.*SNAPSHOT$/
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   *
   * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
   * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
   * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
   * <tt>y.compareTo(x)</tt> throws an exception.)
   *
   * <p>The implementor must also ensure that the relation is transitive:
   * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
   * <tt>x.compareTo(z)&gt;0</tt>.
   *
   * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
   * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
   * all <tt>z</tt>.
   *
   * <p>It is strongly recommended, but <i>not</i> strictly required that
   * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
   * class that implements the <tt>Comparable</tt> interface and violates
   * this condition should clearly indicate this fact.  The recommended
   * language is "Note: this class has a natural ordering that is
   * inconsistent with equals."
   *
   * <p>In the foregoing description, the notation
   * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
   * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
   * <tt>0</tt>, or <tt>1</tt> according to whether the value of
   * <i>expression</i> is negative, zero or positive.
   *
   * @param o the object to be compared.
   * @return a negative integer, zero, or a positive integer as this object
   *          is less than, equal to, or greater than the specified object.
   *
   * @throws NullPointerException if the specified object is null
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this object.
   */
  @Override
  int compareTo(Addon o) {
    return this.equals(o) ? 0 :
        (this.id.equals(o.id) ?
            VERSION_SCHEME.parseVersion(this.version).compareTo(VERSION_SCHEME.parseVersion(o.version)) :
            this.id.compareTo(o.id))
  }
}
