sudo apt-get update
sudo apt-get install -y rpm libfuse2 flatpak flatpak-builder appstream
flatpak remote-add --if-not-exists --user flathub https://dl.flathub.org/repo/flathub.flatpakrepo
