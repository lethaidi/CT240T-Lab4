param([ValidateSet('safe','unsafe','race','test')][string]$Mode = 'safe')
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Path 'build/classes' -Force | Out-Null
    $sources = @(Get-ChildItem 'src/main/java/com/ctu/cinema/*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 --release 17 -d build/classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Bien dich that bai' }
    switch ($Mode) {
        safe { & java -cp build/classes com.ctu.cinema.TicketBookingSystem }
        unsafe { & java -cp build/classes com.ctu.cinema.UnsafeTicketBookingSystem }
        race { & java -cp build/classes com.ctu.cinema.UnsafeTicketBookingSystem --contention }
        test { & java -cp build/classes com.ctu.cinema.TicketBookingSystemTest }
    }
    if ($LASTEXITCODE -ne 0) { throw 'Chuong trinh hoac kiem thu that bai' }
} finally { Pop-Location }
