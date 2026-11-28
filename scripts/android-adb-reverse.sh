#!/usr/bin/env bash
# Rend le backend du PC joignable depuis l'émulateur / un téléphone en USB via « localhost ».
# À relancer après chaque démarrage de l'émulateur.
set -e
adb reverse tcp:8085 tcp:8085   # api-gateway
adb reverse tcp:8180 tcp:8180   # Keycloak
adb reverse tcp:4200 tcp:4200   # redirection après inscription (application Angular)
adb reverse --list
