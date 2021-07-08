#!/bin/bash

echo 'Clean up...'
./scripts/cleanup.sh

echo 'Building...'
./scripts/build-libwally-core.sh

./scripts/build-bc-ur.sh