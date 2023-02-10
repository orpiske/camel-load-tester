TOOLS_HOME=${HOME}/tools
install_path=$(dirname $0)

source  ${install_path}/config/launcher.conf

tmux new-session -d -s "controller" ${TESTER_DIR}/start-controller.sh
