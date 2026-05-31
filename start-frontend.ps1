$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add('http://localhost:8081/')
$listener.Start()
Write-Host '前端服务已启动: http://localhost:8081/'
Write-Host '按 Ctrl+C 停止服务'

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response
        $url = $request.Url.LocalPath
        
        if ($url -eq '/') {
            $url = '/index.html'
        }
        
        $filePath = Join-Path (Get-Location) "frontend$url"
        
        if (Test-Path $filePath) {
            $content = [System.IO.File]::ReadAllBytes($filePath)
            $extension = [System.IO.Path]::GetExtension($filePath)
            
            switch ($extension) {
                '.html' { $response.ContentType = 'text/html; charset=utf-8' }
                '.js'   { $response.ContentType = 'application/javascript; charset=utf-8' }
                '.css'  { $response.ContentType = 'text/css; charset=utf-8' }
                '.json' { $response.ContentType = 'application/json; charset=utf-8' }
                default { $response.ContentType = 'application/octet-stream' }
            }
            
            $response.ContentLength64 = $content.Length
            $response.OutputStream.Write($content, 0, $content.Length)
        } else {
            $response.StatusCode = 404
            $notFound = [System.Text.Encoding]::UTF8.GetBytes('404 Not Found')
            $response.ContentLength64 = $notFound.Length
            $response.OutputStream.Write($notFound, 0, $notFound.Length)
        }
        
        $response.OutputStream.Close()
        $response.Close()
    }
} finally {
    $listener.Stop()
    $listener.Dispose()
}
