#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

$SCRIPT_DIR/local_test_device/simple_mdns_device pr-test de-test testsubtype testkey testvalue
