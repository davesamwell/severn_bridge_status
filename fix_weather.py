#!/usr/bin/env python3
"""Fix weather addition"""
import re

with open('bridge_monitor.py', 'r') as f:
    content = f.read()

# Find where to insert weather display in main()
# Insert after the timestamp print and before "# Fetch data"
old_section = '''    print(f"              {now.strftime('%Y-%m-%d %H:%M:%S')} (UTC)")
    print()
    
    # Fetch data
    xml_data = fetch_closures()'''

new_section = '''    print(f"              {now.strftime('%Y-%m-%d %H:%M:%S')} (UTC)")
    print()
    
    # Fetch and display weather FIRST
    weather_data = fetch_weather()
    weather = parse_weather_data(weather_data) if weather_data else None
    print()
    display_weather(weather)
    
    # Fetch data
    xml_data = fetch_closures()'''

content = content.replace(old_section, new_section)

with open('bridge_monitor.py', 'w') as f:
    f.write(content)

print("✓ Fixed! Weather should now display.")
