$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add('http://localhost:5173/')
$listener.Start()

Write-Host "========================================"
Write-Host "股票量化分析系统"
Write-Host "Server running at http://localhost:5173/"
Write-Host "Press Ctrl+C to stop server"
Write-Host "========================================"

$mimeTypeMap = @{
    '.html' = 'text/html; charset=utf-8'
    '.js'   = 'application/javascript'
    '.css'  = 'text/css'
    '.json' = 'application/json'
    '.png'  = 'image/png'
    '.jpg'  = 'image/jpeg'
    '.gif'  = 'image/gif'
    '.svg'  = 'image/svg+xml'
    '.woff' = 'font/woff'
    '.woff2' = 'font/woff2'
    '.ttf'  = 'font/ttf'
    '.ico'  = 'image/x-icon'
}

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response
        
        $path = $request.Url.LocalPath
        if ($path -eq '/' -or $path -eq '') {
            $path = '/stock-analysis.html'
        }
        
        $filePath = Join-Path (Get-Location) $path.TrimStart('/')
        
        if (Test-Path $filePath -PathType Leaf) {
            $extension = [System.IO.Path]::GetExtension($filePath).ToLower()
            $contentType = 'application/octet-stream'
            if ($mimeTypeMap.ContainsKey($extension)) {
                $contentType = $mimeTypeMap[$extension]
            }
            
            $content = [System.IO.File]::ReadAllBytes($filePath)
            $response.ContentType = $contentType
            $response.ContentLength64 = $content.Length
            $response.StatusCode = 200
            $response.OutputStream.Write($content, 0, $content.Length)
            
            Write-Host "200 $path"
        } else {
            $response.StatusCode = 404
            $errorHtml = [System.Text.Encoding]::UTF8.GetBytes("<h1>404 Not Found</h1>")
            $response.ContentType = 'text/html'
            $response.OutputStream.Write($errorHtml, 0, $errorHtml.Length)
            
            Write-Host "404 $path"
        }
        
        $response.Close()
    }
} finally {
    $listener.Stop()
    $listener.Close()
    Write-Host "Server stopped"
}
