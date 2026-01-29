#!/usr/bin/env python3
import urllib.request
import xml.etree.ElementTree as ET
import ssl

API_KEY = open('api_primary_key.txt').read().strip()
BASE_URL = 'https://api.data.nationalhighways.co.uk/roads/v2.0/closures'
SSL_CONTEXT = ssl._create_unverified_context()

headers = {
    'Ocp-Apim-Subscription-Key': API_KEY,
    'Accept': 'application/xml'
}

req = urllib.request.Request(BASE_URL, headers=headers)
with urllib.request.urlopen(req, timeout=10, context=SSL_CONTEXT) as response:
    xml_data = response.read().decode('utf-8')

root = ET.fromstring(xml_data)
count = 0
for situation in root.findall('.//situation'):
    for record in situation.findall('.//sitRoadOrCarriagewayOrLaneManagement'):
        road_name = record.find('.//roadName')
        if road_name is not None and road_name.text in ['M4', 'M48']:
            location = record.find('.//locationDescription')
            status = record.find('.//validityStatus')
            start = record.find('.//overallStartTime')
            end = record.find('.//overallEndTime')
            comment = record.find('.//comment')
            
            if 'severn' in (location.text if location is not None else '').lower() or \
               ('j1' in (location.text if location is not None else '').lower() or \
                'j2' in (location.text if location is not None else '').lower() or \
                'j21' in (location.text if location is not None else '').lower() or \
                'j22' in (location.text if location is not None else '').lower() or \
                'j23' in (location.text if location is not None else '').lower() or \
                'j24' in (location.text if location is not None else '').lower()):
                count += 1
                print(f'{count}. {road_name.text} - {location.text if location is not None else "N/A"}')
                print(f'   Status: {status.text if status is not None else "N/A"}')
                print(f'   Start: {start.text if start is not None else "N/A"}')
                print(f'   End: {end.text if end is not None else "N/A"}')
                if comment is not None:
                    print(f'   Description: {comment.text}')
                print()
