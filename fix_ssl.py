#!/usr/bin/env python3
"""Fix SSL context for weather API"""

with open('bridge_monitor.py', 'r') as f:
    content = f.read()

# Add SSL context to weather fetch
old = '''    try:
        req = urllib.request.Request(WEATHER_URL)
        with urllib.request.urlopen(req, timeout=10) as response:'''

new = '''    try:
        req = urllib.request.Request(WEATHER_URL)
        with urllib.request.urlopen(req, timeout=10, context=SSL_CONTEXT) as response:'''

content = content.replace(old, new)

with open('bridge_monitor.py', 'w') as f:
    f.write(content)

print("✓ Fixed SSL context for weather API")
