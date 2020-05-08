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
package org.exoplatform.platform.am.cli

import com.beust.jcommander.converters.BaseConverter

/**
 * String -> Conflict Converter for JCommander
 *
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
class ConflictConverter extends BaseConverter<Conflict> {
  public ConflictConverter(String optionName) {
    super(optionName);
  }

/**
 * {@inheritDoc}
 */
  @Override
  public Conflict convert(String value) {
    // ConflictValidator ensures that the value passed here is valid
    Conflict.valueOf(value.toUpperCase().trim())
  }
}