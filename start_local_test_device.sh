#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

$SCRIPT_DIR/local_test_device/simple_mdns_device pr-mdns de-mdns swift-test-subtype swift-txt-key swift-txt-val
