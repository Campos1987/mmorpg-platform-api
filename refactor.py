import os
import glob
import re

# 1. Update .gitignore
gitignore_path = '.gitignore'
with open(gitignore_path, 'r') as f:
    content = f.read()
if '*.pem' not in content:
    with open(gitignore_path, 'a') as f:
        f.write('\n# Ignorar chaves sensiveis\n*.pem\n')

# 2. File moves
moves = [
    ("src/main/java/com/grankain/platformapi/security", "src/main/java/com/grankain/platformapi/infra/security"),
    ("src/main/java/com/grankain/platformapi/util", "src/main/java/com/grankain/platformapi/infra/util"),
    ("src/main/java/com/grankain/platformapi/auth/dto/request/RequestLogin.java", "src/main/java/com/grankain/platformapi/auth/dto/request/LoginRequest.java"),
    ("src/main/java/com/grankain/platformapi/auth/dto/request/RequestRegister.java", "src/main/java/com/grankain/platformapi/auth/dto/request/RegisterRequest.java"),
    ("src/main/java/com/grankain/platformapi/auth/dto/response/ResponseLogin.java", "src/main/java/com/grankain/platformapi/auth/dto/response/LoginResponse.java"),
    ("src/main/java/com/grankain/platformapi/auth/dto/response/ResponseRegister.java", "src/main/java/com/grankain/platformapi/auth/dto/response/RegisterResponse.java"),
    ("src/main/java/com/grankain/platformapi/infra/exception/dto/ApiErrorException.java", "src/main/java/com/grankain/platformapi/infra/exception/dto/ApiErrorResponse.java"),
    ("src/main/java/com/grankain/platformapi/dashboard/controller/Dashboard.java", "src/main/java/com/grankain/platformapi/dashboard/controller/DashboardController.java"),
    ("src/main/java/com/grankain/platformapi/dashboard/service/UserAccount.java", "src/main/java/com/grankain/platformapi/dashboard/service/UserAccountService.java"),
    ("src/main/java/com/grankain/platformapi/auth/domain/login/LoginAttemptService.java", "src/main/java/com/grankain/platformapi/auth/service/LoginAttemptService.java")
]

for src, dst in moves:
    if os.path.exists(src):
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        # Using git mv if we want, but simple mv is fine
        os.system(f'git mv {src} {dst} 2>/dev/null || mv {src} {dst}')

# 3. File content replacements
java_files = glob.glob('src/main/java/**/*.java', recursive=True)

global_replacements = {
    "package com.grankain.platformapi.security;": "package com.grankain.platformapi.infra.security;",
    "import com.grankain.platformapi.security.": "import com.grankain.platformapi.infra.security.",
    
    "package com.grankain.platformapi.util;": "package com.grankain.platformapi.infra.util;",
    "import com.grankain.platformapi.util.": "import com.grankain.platformapi.infra.util.",
    
    "RequestLogin": "LoginRequest",
    "RequestRegister": "RegisterRequest",
    "ResponseLogin": "LoginResponse",
    "ResponseRegister": "RegisterResponse",
    
    "ApiErrorException": "ApiErrorResponse",
    
    "class Dashboard ": "class DashboardController ",
    "public Dashboard(": "public DashboardController(",
    
    "class UserAccount ": "class UserAccountService ",
    "public UserAccount(": "public UserAccountService(",
    "UserAccount userAccount": "UserAccountService userAccountService",
    
    "import com.grankain.platformapi.dashboard.service.UserAccount;": "import com.grankain.platformapi.dashboard.service.UserAccountService;"
}

for filepath in java_files:
    if not os.path.isfile(filepath): continue
    with open(filepath, 'r') as f:
        content = f.read()
    
    new_content = content
    for old_str, new_str in global_replacements.items():
        new_content = new_content.replace(old_str, new_str)
        
    # File-specific replacements
    if filepath.endswith("LoginAttemptService.java"):
        new_content = new_content.replace("package com.grankain.platformapi.auth.domain.login;", "package com.grankain.platformapi.auth.service;")
        
    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)
