#!/bin/bash
###############################################################################
# Copyright (c) 2014 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Alexandre Montplaisir - Initial API and implementation
###############################################################################

# Synchronize the project settings for all plugins
# (by copying the contents of tmf.core's settings)

# Plugins from which we will copy the settings
RUNTIME_REFERENCE="tmf/org.eclipse.tracecompass.tmf.core"
TEST_REFERENCE="tmf/org.eclipse.tracecompass.tmf.core.tests"

RUNTIME_FILES=$RUNTIME_REFERENCE/.settings/*.prefs
TEST_FILES=$TEST_REFERENCE/.settings/*.prefs

# Runtime plugins
for DIR in */*.core */*.ui */*.branding doc/org.eclipse.tracecompass.examples
do
  # Skip non-directories
  if [ ! -d $DIR ]; then
    continue
  fi

  # Don't copy over the same files
  if [ "$DIR" == "$RUNTIME_REFERENCE" ]; then
    continue
  fi

  for FILE in $RUNTIME_FILES
  do
    cp $FILE "$DIR/.settings/"
  done
done


# Test plugins
for DIR in */*.tests */*.alltests
do
  # Skip non-directories
  if [ ! -d $DIR ]; then
    continue
  fi

  # Don't copy over the same files
  if [ "$DIR" == "$TEST_REFERENCE" ]; then
    continue
  fi

  for FILE in $TEST_FILES
  do
    cp $FILE "$DIR/.settings/"
  done
done
