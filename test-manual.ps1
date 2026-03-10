# test-manual.ps1
# Script de tests manuels reproductibles pour le projet

Write-Host "=== TESTS MANUELS DU FLUX COMPLET ===" -ForegroundColor Cyan
Write-Host "Prerequis: L'application doit tourner sur http://localhost:8080" -ForegroundColor Yellow
Write-Host ""

$baseUrl = "http://localhost:8080"
$passed = 0
$failed = 0

function Test-Endpoint {
    param($Name, $ScriptBlock)
    Write-Host "[$Name] " -NoNewline
    try {
        $result = & $ScriptBlock
        if ($result) {
            Write-Host "OK" -ForegroundColor Green
            $script:passed++
        } else {
            Write-Host "ECHEC" -ForegroundColor Red
            $script:failed++
        }
    } catch {
        Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

# --- 1. Login student1 ---
Test-Endpoint "Login student1" {
    $body = '{"identifier":"student1","password":"password"}'
    $resp = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
    $script:studentToken = $resp.value
    Write-Host " Token=$studentToken" -NoNewline
    return $studentToken -ne $null
}

# --- 2. Validate token ---
Test-Endpoint "Validate token" {
    $resp = Invoke-WebRequest -Uri "$baseUrl/auth/validate/$studentToken" -Method GET -UseBasicParsing
    return $resp.StatusCode -eq 200
}

# --- 3. Login admin ---
Test-Endpoint "Login admin" {
    $body = '{"identifier":"admin","password":"adminpass"}'
    $resp = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
    $script:adminToken = $resp.value
    Write-Host " Token=$adminToken" -NoNewline
    return $adminToken -ne $null
}

# --- 4. Get products ---
Test-Endpoint "Get products" {
    $resp = Invoke-RestMethod -Uri "$baseUrl/products" -Method GET
    Write-Host " Count=$($resp.Count)" -NoNewline
    return $resp.Count -ge 2
}

# --- 5. Register nouveau user ---
$timestamp = Get-Date -Format "HHmmss"
$testEmail = "test-$timestamp@demo.com"
Test-Endpoint "Register $testEmail" {
    $body = "{`"email`":`"$testEmail`",`"password`":`"pass123`"}"
    $resp = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -UseBasicParsing
    return $resp.StatusCode -eq 201
}

# --- 6. Register user existant (doit echouer 409) ---
Test-Endpoint "Register existant => 409" {
    try {
        $body = '{"email":"student1","password":"pass"}'
        Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -UseBasicParsing -ErrorAction Stop
        return $false
    } catch {
        return $_.Exception.Response.StatusCode.value__ -eq 409
    }
}

# --- 7. Validate faux token (doit echouer) ---
Test-Endpoint "Validate faux token => 401" {
    try {
        Invoke-WebRequest -Uri "$baseUrl/auth/validate/fake-token-123" -Method GET -UseBasicParsing -ErrorAction Stop
        return $false
    } catch {
        return $_.Exception.Response.StatusCode.value__ -eq 401
    }
}

# --- 8. Login mauvais password (doit echouer) ---
Test-Endpoint "Login mauvais password => 401" {
    try {
        $body = '{"identifier":"student1","password":"wrong"}'
        Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -UseBasicParsing -ErrorAction Stop
        return $false
    } catch {
        return $_.Exception.Response.StatusCode.value__ -eq 401
    }
}

# --- 9. Get users ---
Test-Endpoint "Get users" {
    $resp = Invoke-RestMethod -Uri "$baseUrl/auth/users" -Method GET
    $hasStudent = ($resp | Where-Object { $_.identifier -eq "student1" }) -ne $null
    $hasAdmin = ($resp | Where-Object { $_.identifier -eq "admin" }) -ne $null
    Write-Host " student1=$hasStudent admin=$hasAdmin" -NoNewline
    return $hasStudent -and $hasAdmin
}

# --- 10. Logout ---
Test-Endpoint "Logout student1" {
    Invoke-WebRequest -Uri "$baseUrl/auth/logout/$studentToken" -Method DELETE -UseBasicParsing | Out-Null
    return $true
}

# --- 11. Validate apres logout (doit echouer) ---
Test-Endpoint "Validate apres logout => 401" {
    try {
        Invoke-WebRequest -Uri "$baseUrl/auth/validate/$studentToken" -Method GET -UseBasicParsing -ErrorAction Stop
        return $false
    } catch {
        return $_.Exception.Response.StatusCode.value__ -eq 401
    }
}

# --- Resume ---
Write-Host ""
Write-Host "=== RESUME ===" -ForegroundColor Cyan
Write-Host "Passes: $passed" -ForegroundColor Green
Write-Host "Echecs: $failed" -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "Green" })

if ($failed -eq 0) {
    Write-Host "`nTous les tests sont passes!" -ForegroundColor Green
} else {
    Write-Host "`nCertains tests ont echoue." -ForegroundColor Red
}
