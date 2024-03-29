#!/bin/bash

set -e

function run {
    local config=$1
    shift
    local opts=$*
    ./tcp_tunnel_device_macos -H ./config/$config --random-ports $opts 2>&1 > /tmp/tunnel-$config
}

#find ./config -type d -name state -exec git checkout {} \; || true

run localPairLocalOpen &
run localPairLocalInitial &
run localPairPasswordOpen &
run localPasswordPairingDisabledConfig &
run localPasswordInvite &

./simple_mdns_device_macos pr-mdns de-mdns swift-test-subtype swift-txt-key swift-txt-val

wait
