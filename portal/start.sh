#!/bin/bash
rm -rf ./build
./sync.sh &
node scripts/start.js