#!/bin/bash
# Wrapper script to use nerdctl inside colima VM
colima ssh -- nerdctl "$@"
