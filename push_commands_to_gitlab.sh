#!/bin/bash

BASE64=$(cat COMMANDS.md | base64 -w 0) # do not wrap base64, which would confuse gitlab
generate_json()
{
cat <<EOF
{
    "branch": "master",
    "author_email": "noreply@thisbetternotbeadomainname.com",
    "author_name": "HeyBanditoz",
    "content": "$BASE64",
    "commit_message": "[automated] update command list [ci skip]",
    "encoding": "base64"
}
EOF
}

cmp COMMANDS.md COMMANDS_OLD.md || EXIT_CODE=$?

if [[ $EXIT_CODE -eq 0 ]]; then
    echo "same files, no commands changed"
    exit 0
elif [[ $EXIT_CODE -ne 0 ]]; then
    echo "executing curl"
    curl -i -X PUT -H "PRIVATE-TOKEN: $(cat token)" -H "Content-Type: application/json" -d "$(generate_json)" "https://gitlab.com/api/v4/projects/13974445/repository/files/COMMANDS.md"
fi

#rm COMMANDS_OLD.md
