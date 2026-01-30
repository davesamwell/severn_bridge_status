#!/usr/bin/env python3
"""
Severn Bridge Monitor - Proof of Concept
Queries the National Highways API for M4/M48 Severn Bridge status
"""

import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
import ssl
import hashlib
import json

# API Configuration
API_KEY = open('api_primary_key.txt').read().strip()
BASE_URL = "https://api.data.nationalhighways.co.uk/roads/v2.0/closures"
# M48 Severn Bridge coordinates for weather
WEATHER_URL = "https://api.open-meteo.com/v1/forecast?latitude=51.61&longitude=-2.64&hourly=precipitation_probability,windgusts_10m&timezone=Europe/London&forecast_days=1&current_weather=true"

# SSL context
SSL_CONTEXT = ssl._create_unverified_context()

# Simple caching
_cache = {'hash': None, 'data': None}
_weather_cache = {'timestamp': None, 'data': None}
WEATHER_CACHE_DURATION = 1800  # 30 minutes in seconds

# ANSI Color codes
COLOR_GREEN = '\033[92m'
COLOR_YELLOW = '\033[93m'
COLOR_RED = '\033[91m'
COLOR_RESET = '\033[0m'
COLOR_BOLD = '\033[1m'

# Severn Bridge coordinates (approximate)
# M48 Severn Bridge: 51.61°N, 2.64°W
# M4 Prince of Wales Bridge: 51.57°N, 2.64°W
SEVERN_BRIDGE_AREA = {
    'lat_min': 51.55,
    'lat_max': 51.65,
    'lon_min': -2.75,
    'lon_max': -2.55
}


def fetch_weather():
    """Fetch weather data from Open-Meteo API (no API key needed)"""
    now = datetime.now(timezone.utc)
    
    # Check cache
    if _weather_cache['timestamp'] is not None and _weather_cache['data'] is not None:
        cache_age = (now - _weather_cache['timestamp']).total_seconds()
        if cache_age < WEATHER_CACHE_DURATION:
            print(f"Using cached weather data ({int(cache_age/60)} minutes old)")
            return _weather_cache['data']
    
    print("Fetching weather from Open-Meteo API...")
    
    try:
        req = urllib.request.Request(WEATHER_URL)
        with urllib.request.urlopen(req, timeout=10, context=SSL_CONTEXT) as response:
            if response.status == 200:
                print(f"✓ Weather API call successful")
                data = json.loads(response.read().decode('utf-8'))
                
                # Cache the result
                _weather_cache['timestamp'] = now
                _weather_cache['data'] = data
                
                return data
            else:
                print(f"✗ Weather API call failed (Status: {response.status})")
                return None
    except Exception as e:
        print(f"✗ Weather API call failed: {e}")
        return None

def parse_weather_data(weather_data):
    """Parse Open-Meteo weather response and extract current conditions"""
    if not weather_data:
        return None
    
    # Get current temperature from current_weather field (more accurate)
    temperature = None
    wind_speed_kmh = None
    if 'current_weather' in weather_data:
        temperature = weather_data['current_weather'].get('temperature')
        wind_speed_kmh = weather_data['current_weather'].get('windspeed')
    
    wind_speed_mph = wind_speed_kmh * 0.621371 if wind_speed_kmh is not None else None
    
    # Get today's max rain probability and max gust from hourly data
    rain_prob = None
    rain_time = None
    max_gust_mph = None
    gust_time = None
    if 'hourly' in weather_data:
        hourly = weather_data['hourly']
        
        # Get max rain probability for today (shows highest chance throughout the day)
        if 'precipitation_probability' in hourly and hourly['precipitation_probability']:
            rain_probs = hourly['precipitation_probability']
            rain_prob = max(rain_probs)
            # Find the time when max rain probability occurs
            max_idx = rain_probs.index(rain_prob)
            if 'time' in hourly and max_idx < len(hourly['time']):
                rain_time = hourly['time'][max_idx]
        
        # Get max wind gust for today
        if 'windgusts_10m' in hourly and hourly['windgusts_10m']:
            gusts = hourly['windgusts_10m']
            max_gust_kmh = max(gusts)
            max_gust_mph = max_gust_kmh * 0.621371
            # Find the time when max gust occurs
            max_idx = gusts.index(max_gust_kmh)
            if 'time' in hourly and max_idx < len(hourly['time']):
                gust_time = hourly['time'][max_idx]
    
    return {
        'temperature': temperature,
        'rain_probability': rain_prob,
        'rain_time': rain_time,
        'wind_speed_mph': wind_speed_mph,
        'max_gust_mph': max_gust_mph,
        'gust_time': gust_time
    }

def get_wind_risk_level(wind_mph):
    """Determine wind risk level based on mph"""
    if wind_mph is None:
        return 'unknown', COLOR_RESET
    if wind_mph >= 41:
        return 'HIGH RISK - Likely closure', COLOR_RED
    elif wind_mph >= 26:
        return 'MONITOR - Possible restrictions', COLOR_YELLOW
    else:
        return 'Safe', COLOR_GREEN

def display_weather(weather):
    """Display weather information with color coding"""
    if not weather:
        print("❌ Weather data unavailable")
        return
    
    print("=" * 70)
    print("🌤️  SEVERN BRIDGE WEATHER - TODAY")
    print("=" * 70)
    print()
    
    if weather['temperature'] is not None:
        print(f"🌡️  Current Temperature: {weather['temperature']:.1f}°C")
    
    if weather['rain_probability'] is not None:
        rain_text = f"🌧️  Max Rain Probability Today: {weather['rain_probability']}%"
        if weather.get('rain_time'):
            # Extract just the hour from ISO timestamp (e.g., "2026-01-29T14:00" -> "14:00")
            time_str = weather['rain_time'].split('T')[1] if 'T' in weather['rain_time'] else weather['rain_time']
            rain_text += f" (at {time_str})"
        print(rain_text)
    
    print()
    
    if weather['wind_speed_mph'] is not None:
        risk_level, color = get_wind_risk_level(weather['wind_speed_mph'])
        print(f"💨 Current Wind Speed: {color}{weather['wind_speed_mph']:.1f} mph{COLOR_RESET}")
        print(f"   Status: {color}{COLOR_BOLD}{risk_level}{COLOR_RESET}")
    
    if weather['max_gust_mph'] is not None:
        risk_level, color = get_wind_risk_level(weather['max_gust_mph'])
        print()
        gust_text = f"🌪️  Max Wind Gust Today: {color}{weather['max_gust_mph']:.1f} mph{COLOR_RESET}"
        if weather.get('gust_time'):
            time_str = weather['gust_time'].split('T')[1] if 'T' in weather['gust_time'] else weather['gust_time']
            gust_text += f" (at {time_str})"
        print(gust_text)
        print(f"   Status: {color}{COLOR_BOLD}{risk_level}{COLOR_RESET}")
    
    print()
    print("Wind Risk Levels:")
    print(f"  {COLOR_GREEN}• 0-25 mph: Safe{COLOR_RESET}")
    print(f"  {COLOR_YELLOW}• 26-40 mph: Monitor - possible restrictions{COLOR_RESET}")
    print(f"  {COLOR_RED}• 41+ mph: High risk - likely closure{COLOR_RESET}")
    print()

def fetch_closures():
    """Fetch closure data from National Highways API"""
    headers = {
        'Ocp-Apim-Subscription-Key': API_KEY,
        'Accept': 'application/xml'  # API seems to default to XML
    }
    
    print("Fetching data from National Highways API...")
    
    req = urllib.request.Request(BASE_URL, headers=headers)
    
    try:
        with urllib.request.urlopen(req, timeout=10, context=SSL_CONTEXT) as response:
            if response.status == 200:
                print(f"✓ API call successful (Status: {response.status})")
                return response.read().decode('utf-8')
            else:
                print(f"✗ API call failed (Status: {response.status})")
                return None
    except Exception as e:
        print(f"✗ API call failed: {e}")
        return None

def parse_xml_closures(xml_data):
    """Parse XML response and extract relevant closure information"""
    
    # Check cache
    data_hash = hashlib.md5(xml_data.encode()).hexdigest()
    if _cache['hash'] == data_hash and _cache['data'] is not None:
        print("Using cached parsed data (no changes detected)")
        return _cache['data']
    
    root = ET.fromstring(xml_data)
    
    # Define namespace (if present)
    ns = {}
    
    closures = []
    situations = root.findall('.//situation', ns)
    
    print(f"Found {len(situations)} total situations")
    
    for situation in situations:
        for record in situation.findall('.//sitRoadOrCarriagewayOrLaneManagement', ns):
            # Extract key information
            road_name = record.find('.//roadName')
            
            # Early filtering: Skip if not M4 or M48
            road = road_name.text if road_name is not None else "Unknown"
            if road not in ['M4', 'M48']:
                continue
            
            location_desc = record.find('.//locationDescription')
            comment = record.find('.//comment')
            validity_status = record.find('.//validityStatus')
            start_time = record.find('.//overallStartTime')
            end_time = record.find('.//overallEndTime')
            pos_list = record.find('.//posList')
            probability = record.find('.//probabilityOfOccurrence')
            cause_type = record.find('.//causeType')
            management_type = record.find('.//roadOrCarriagewayOrLaneManagementType/value')
            
            # Get text values safely
            location = location_desc.text if location_desc is not None else "Unknown"
            description = comment.text if comment is not None else "No description"
            status = validity_status.text if validity_status is not None else "Unknown"
            prob = probability.text if probability is not None else "unknown"
            cause = cause_type.text if cause_type is not None else "unknown"
            
            # Extract direction information
            direction_elem = record.find('.//directionOnLinearSection')
            direction = direction_elem.text if direction_elem is not None else "unknown"
            
            closure = {
                'road': road,
                'location': location,
                'description': description,
                'status': status,
                'probability': prob,
                'cause': cause,
                'start': start_time.text if start_time is not None else None,
                'end': end_time.text if end_time is not None else None,
                'coordinates': pos_list.text if pos_list is not None else None,
                'direction': direction
            }
            closures.append(closure)
    
    # Cache the result
    _cache['hash'] = data_hash
    _cache['data'] = closures
    
    return closures

def check_severn_bridge(closure):
    """Check if closure is near Severn Bridge based on coordinates or location"""
    location_lower = closure['location'].lower()
    road = closure['road']
    
    # M48 Severn Bridge specific - Junction 1 and 2 only
    if road == 'M48':
        if any(j in location_lower for j in ['j1', 'j2', 'junction 1', 'junction 2', 'severn']):
            return True
    
    # M4 Second Severn Crossing / Prince of Wales Bridge
    # Wales side: J23, J24 (westbound into Wales)
    # England side: J21, J22 (eastbound into England)
    if road == 'M4':
        # Check for Severn Bridge specific junctions
        if any(j in location_lower for j in ['j21', 'j22', 'j23', 'j24', 
                                               'junction 21', 'junction 22', 
                                               'junction 23', 'junction 24', 
                                               'severn']):
            return True
        
        # Check for "Wales" or England/Wales border mentions
        if 'wales' in location_lower or 'welsh border' in location_lower:
            return True
    
    # Check coordinates if available (definitive location check)
    if closure['coordinates']:
        try:
            coords = closure['coordinates'].split()
            # Coordinates are in lat lon pairs
            for i in range(0, len(coords)-1, 2):
                lat = float(coords[i])
                lon = float(coords[i+1])
                
                if (SEVERN_BRIDGE_AREA['lat_min'] <= lat <= SEVERN_BRIDGE_AREA['lat_max'] and
                    SEVERN_BRIDGE_AREA['lon_min'] <= lon <= SEVERN_BRIDGE_AREA['lon_max']):
                    return True
        except (ValueError, IndexError):
            pass
    
    return False

def is_currently_active(closure):
    """
    Determine if a closure is CURRENTLY ACTIVE (happening right now)
    
    Status values from API:
    - 'active': Closure has started (confirmed)
    - 'planned': Closure is scheduled but not yet started
    - 'suspended': Closure is cancelled/postponed
    
    Returns: (is_active, reason)
    """
    now = datetime.now(timezone.utc)
    status = closure['status'].lower()
    
    # If status is explicitly 'active', the closure is happening NOW
    if status == 'active':
        return True, "ACTIVE (confirmed by operator)"
    
    # If suspended, it's not active
    if status == 'suspended':
        return False, "Suspended/Cancelled"
    
    # For 'planned' status, check if we're within the time window
    if status == 'planned':
        if closure['start'] and closure['end']:
            try:
                start_time = datetime.fromisoformat(closure['start'].replace('Z', '+00:00'))
                end_time = datetime.fromisoformat(closure['end'].replace('Z', '+00:00'))
                
                if start_time <= now <= end_time:
                    # We're in the planned time window
                    return True, "ACTIVE (within planned time window)"
                elif now < start_time:
                    # Future closure
                    return False, f"Planned for later (starts {start_time.strftime('%H:%M %Z')})"
                else:
                    # Past closure
                    return False, "Past closure"
            except:
                pass
        return False, "Planned (time uncertain)"
    
    return False, f"Unknown status: {status}"

def clean_description(description):
    """Remove mile marker references like 201/5-196/0 for better readability"""
    import re
    # Remove patterns like "201/5-196/0" or "201/5" at the end
    description = re.sub(r'\s*\d+/\d+-\d+/\d+\s*', ' ', description)  # Range markers
    description = re.sub(r'\s*\d+/\d+\s*$', '', description)           # Single marker at end
    return re.sub(r'\s+', ' ', description).strip()                    # Clean extra spaces

def analyze_directional_status(closures, direction):
    """
    Analyze status for a specific direction
    direction: 'eastBound' or 'westBound'
    """
    directional_closures = [c for c in closures if 
                           c.get('direction', '').lower() == direction.lower() or 
                           c.get('direction', '').lower() == 'bothdirections']
    
    active_closures = [c for c in directional_closures if c['is_active']]
    
    if not active_closures:
        return "OPEN", directional_closures
    
    # Check for full closure
    for closure in active_closures:
        if 'carriageway closure' in closure['description'].lower():
            return "CLOSED", directional_closures
    
    return "RESTRICTED", directional_closures

def get_bridge_current_status(closures):
    """
    Determine the CURRENT STATUS of each Severn Bridge
    Returns: dict with M4 and M48 status including directional info
    """
    m4_status = {"bridge": "M4 Prince of Wales Bridge", "status": "OPEN", "closures": [], 
                 "eastbound": "OPEN", "westbound": "OPEN"}
    m48_status = {"bridge": "M48 Severn Bridge", "status": "OPEN", "closures": [],
                  "eastbound": "OPEN", "westbound": "OPEN"}
    
    for closure in closures:
        if check_severn_bridge(closure):
            is_active, reason = is_currently_active(closure)
            
            closure_info = {
                'location': closure['location'],
                'description': clean_description(closure['description']),
                'is_active': is_active,
                'reason': reason,
                'status': closure['status'],
                'probability': closure['probability'],
                'cause': closure['cause'],
                'start': closure['start'],
                'end': closure['end'],
                'direction': closure.get('direction', 'unknown')
            }
            
            # Categorize by bridge
            if 'M48' in closure['road'] or 'M48' in closure['location']:
                m48_status['closures'].append(closure_info)
                if is_active:
                    m48_status['status'] = "CLOSED" if 'carriageway closure' in closure['description'].lower() else "RESTRICTED"
            elif 'M4' in closure['road'] or 'M4' in closure['location']:
                m4_status['closures'].append(closure_info)
                if is_active:
                    m4_status['status'] = "CLOSED" if 'carriageway closure' in closure['description'].lower() else "RESTRICTED"
    
    # Analyze directional status
    m48_east_status, _ = analyze_directional_status(m48_status['closures'], 'eastBound')
    m48_west_status, _ = analyze_directional_status(m48_status['closures'], 'westBound')
    m48_status['eastbound'] = m48_east_status
    m48_status['westbound'] = m48_west_status
    
    m4_east_status, _ = analyze_directional_status(m4_status['closures'], 'eastBound')
    m4_west_status, _ = analyze_directional_status(m4_status['closures'], 'westBound')
    m4_status['eastbound'] = m4_east_status
    m4_status['westbound'] = m4_west_status
    
    return m4_status, m48_status

def main():
    print("=" * 70)
    print("🌉 SEVERN BRIDGES - CURRENT STATUS")
    print("=" * 70)
    now = datetime.now(timezone.utc)
    local_now = datetime.now()
    print(f"Current time: {local_now.strftime('%Y-%m-%d %H:%M:%S')} (Local)")
    print(f"              {now.strftime('%Y-%m-%d %H:%M:%S')} (UTC)")
    print()
    
    # Fetch and display weather FIRST
    weather_data = fetch_weather()
    weather = parse_weather_data(weather_data) if weather_data else None
    print()
    display_weather(weather)
    
    # Fetch data
    xml_data = fetch_closures()
    if not xml_data:
        print("❌ Failed to fetch data")
        return
    
    print()
    
    # Parse closures
    closures = parse_xml_closures(xml_data)
    print(f"Found {len(closures)} M4/M48 closures\n")
    
    # Get current status of both bridges
    m4_status, m48_status = get_bridge_current_status(closures)
    
    # Display CURRENT STATUS prominently
    print("=" * 70)
    print("⚠️  CURRENT STATUS - RIGHT NOW")
    print("=" * 70)
    print()
    
    # M48 Status
    status_symbol_48 = "🟢" if m48_status['status'] == "OPEN" else "🔴" if m48_status['status'] == "CLOSED" else "🟡"
    print(f"{status_symbol_48} M48 SEVERN BRIDGE (Original Bridge, 1966)")
    print(f"   Overall Status: {m48_status['status']}")
    
    # Directional Status
    east_symbol = "🟢" if m48_status['eastbound'] == "OPEN" else "🔴" if m48_status['eastbound'] == "CLOSED" else "🟡"
    west_symbol = "🟢" if m48_status['westbound'] == "OPEN" else "🔴" if m48_status['westbound'] == "CLOSED" else "🟡"
    print(f"   → Eastbound (Wales → England): {east_symbol} {m48_status['eastbound']}")
    print(f"   ← Westbound (England → Wales): {west_symbol} {m48_status['westbound']}")
    if m48_status['closures']:
        for closure in m48_status['closures']:
            direction_str = ""
            if 'direction' in closure:
                direction = closure.get('direction', 'unknown').lower()
                if direction == 'eastbound':
                    direction_str = "→ Eastbound: "
                elif direction == 'westbound':
                    direction_str = "← Westbound: "
                elif direction == 'bothdirections':
                    direction_str = "↔️ Both directions: "
            
            if closure['is_active']:
                print(f"   ⚠️  ACTIVE CLOSURE - {direction_str}{closure['location']}")
                print(f"      {closure['description']}")
                print(f"      Reason: {closure['reason']}")
                print(f"      Cause: {closure['cause']}")
                if closure['end']:
                    try:
                        end_time = datetime.fromisoformat(closure['end'].replace('Z', '+00:00'))
                        print(f"      Until: {end_time.strftime('%Y-%m-%d %H:%M %Z')}")
                    except:
                        pass
            else:
                print(f"   ℹ️  Planned: {closure['description']}")
                print(f"      {closure['reason']}")
                if closure['start']:
                    try:
                        start_time = datetime.fromisoformat(closure['start'].replace('Z', '+00:00'))
                        print(f"      From: {start_time.strftime('%Y-%m-%d %H:%M %Z')}")
                    except:
                        pass
                if closure['end']:
                    try:
                        end_time = datetime.fromisoformat(closure['end'].replace('Z', '+00:00'))
                        print(f"      Until: {end_time.strftime('%Y-%m-%d %H:%M %Z')}")
                    except:
                        pass
    else:
        print(f"   ✓ No closures or restrictions")
    print()
    
    # M4 Status
    status_symbol_4 = "🟢" if m4_status['status'] == "OPEN" else "🔴" if m4_status['status'] == "CLOSED" else "🟡"
    print(f"{status_symbol_4} M4 PRINCE OF WALES BRIDGE (Second Severn Crossing, 1996)")
    print(f"   Overall Status: {m4_status['status']}")
    
    # Directional Status
    east_symbol = "🟢" if m4_status['eastbound'] == "OPEN" else "🔴" if m4_status['eastbound'] == "CLOSED" else "🟡"
    west_symbol = "🟢" if m4_status['westbound'] == "OPEN" else "🔴" if m4_status['westbound'] == "CLOSED" else "🟡"
    print(f"   → Eastbound (Wales → England): {east_symbol} {m4_status['eastbound']}")
    print(f"   ← Westbound (England → Wales): {west_symbol} {m4_status['westbound']}")
    if m4_status['closures']:
        for closure in m4_status['closures']:
            direction_str = ""
            if 'direction' in closure:
                direction = closure.get('direction', 'unknown').lower()
                if direction == 'eastbound':
                    direction_str = "→ Eastbound: "
                elif direction == 'westbound':
                    direction_str = "← Westbound: "
                elif direction == 'bothdirections':
                    direction_str = "↔️ Both directions: "
            
            if closure['is_active']:
                print(f"   ⚠️  ACTIVE CLOSURE - {direction_str}{closure['location']}")
                print(f"      {closure['description']}")
                if closure['start']:
                    try:
                        start_time = datetime.fromisoformat(closure['start'].replace('Z', '+00:00'))
                        print(f"      From: {start_time.strftime('%Y-%m-%d %H:%M %Z')}")
                    except:
                        pass
                if closure['end']:
                    try:
                        end_time = datetime.fromisoformat(closure['end'].replace('Z', '+00:00'))
                        print(f"      Until: {end_time.strftime('%Y-%m-%d %H:%M %Z')}")
                    except:
                        pass
                print(f"      {closure['description']}")
                print(f"      Reason: {closure['reason']}")
                print(f"      Cause: {closure['cause']}")
                if closure['end']:
                    try:
                        end_time = datetime.fromisoformat(closure['end'].replace('Z', '+00:00'))
                        print(f"      Until: {end_time.strftime('%Y-%m-%d %H:%M %Z')}")
                    except:
                        pass
            else:
                print(f"   ℹ️  Planned: {closure['description']}")
                print(f"      {closure['reason']}")
    else:
        print(f"   ✓ No closures or restrictions")
    
    # Show upcoming planned closures for Severn Bridge area
    print()
    print("=" * 70)
    print("📅 UPCOMING PLANNED CLOSURES (Severn Bridge Area)")
    print("=" * 70)
    
    upcoming = []
    for closure in closures:
        if check_severn_bridge(closure):
            is_active, reason = is_currently_active(closure)
            if not is_active and closure['status'].lower() == 'planned':
                upcoming.append(closure)
    
    if upcoming:
        for idx, closure in enumerate(upcoming, 1):
            print(f"\n{idx}. {closure['road']} - {closure['location']}")
            print(f"   {clean_description(closure['description'])}")
            if closure['start']:
                try:
                    start_time = datetime.fromisoformat(closure['start'].replace('Z', '+00:00'))
                    print(f"   Starts: {start_time.strftime('%Y-%m-%d %H:%M %Z')}")
                except:
                    print(f"   Starts: {closure['start']}")
            if closure['end']:
                try:
                    end_time = datetime.fromisoformat(closure['end'].replace('Z', '+00:00'))
                    print(f"   Ends: {end_time.strftime('%Y-%m-%d %H:%M %Z')}")
                except:
                    print(f"   Ends: {closure['end']}")
    else:
        print("\n✓ No upcoming planned closures")
    
    print("\n" + "=" * 70)
    print("ℹ️  NOTES:")
    print("   • 'active' status = closure confirmed by operator")
    print("   • 'planned' status = scheduled closure, may not have started yet")
    print("   • Ad-hoc closures (e.g., high winds) appear with 'active' status")
    print("   • Check 'cause' field for reason (e.g., poorEnvironment for weather)")
    print("   • Weather data updates every 30 minutes")
    print("=" * 70)

if __name__ == "__main__":
    main()
