#!/usr/bin/env python3
import urllib.request
import json
import ssl
from datetime import datetime

# Check what Open-Meteo is actually giving us
url = 'https://api.open-meteo.com/v1/forecast?latitude=51.64&longitude=-2.67&hourly=temperature_2m,precipitation_probability,windspeed_10m,windgusts_10m&timezone=Europe/London&forecast_days=1&current_weather=true'

ssl_context = ssl._create_unverified_context()
req = urllib.request.Request(url)

with urllib.request.urlopen(req, timeout=10, context=ssl_context) as response:
    data = json.loads(response.read().decode('utf-8'))

print('=' * 70)    
print('OPEN-METEO API DEBUG')
print('=' * 70)
print(f'Coordinates: {data.get("latitude")}, {data.get("longitude")}')
print(f'Timezone: {data.get("timezone")}')
print()

if 'current_weather' in data:
    cw = data['current_weather']
    print(f'Current Weather API data:')
    print(f'  Temperature: {cw.get("temperature")}°C')
    print(f'  Wind Speed: {cw.get("windspeed")} km/h')
    print(f'  Time: {cw.get("time")}')
print()

print('Hourly forecast (today):')
times = data['hourly']['time']
temps = data['hourly']['temperature_2m']
rain_probs = data['hourly']['precipitation_probability']
winds = data['hourly']['windspeed_10m']
gusts = data['hourly']['windgusts_10m']

now = datetime.now()
print(f'Current time: {now.strftime("%Y-%m-%d %H:%M")}')
print()

for i in range(min(24, len(times))):
    print(f'{times[i]}: {temps[i]:5.1f}°C, Rain: {rain_probs[i]:3d}%, Wind: {winds[i]:5.1f} km/h, Gust: {gusts[i]:5.1f} km/h')

print()
print(f'Today Max Rain Probability: {max(rain_probs)}%')
print(f'Today Max Gust: {max(gusts):.1f} km/h ({max(gusts)*0.621371:.1f} mph)')
