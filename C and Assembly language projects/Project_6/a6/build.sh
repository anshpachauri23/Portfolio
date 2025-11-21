#!/bin/bash

if [ $# -ne 1 ]; then
  echo "Require assembly file."
  exit 42
fi

src=$1

if [ ! -f $src ]; then
  echo "Did not find file $src. Aborting ..."
  exit 42
fi


echo "Processing assembly file: $src ..."

OBJFILE=$src.o
BINFILE=$src.bin

as $src -o $OBJFILE
ld $OBJFILE -o $BINFILE
  
if [ ! -x $BINFILE ]; then
  echo "$BINFILE not found. Aborting ..."
  exit 42
fi

echo "Calling binary file $BINFILE..."
./$BINFILE

echo "Result returned: $?"
  
