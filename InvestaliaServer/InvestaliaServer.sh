cd $1
sh startMain.sh investalia &
cd $2
cd tools
./emulator -avd $3
