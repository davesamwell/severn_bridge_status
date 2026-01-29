# Debug Mode Quick Reference

## 🛠️ Activating Debug Mode

**Long-press** the "Severn Bridge Status" title → Select scenario

## 📋 Scenario Quick Reference

### Scenario 1: ALL OPEN ✅
```
M48: 🟢 OPEN (both directions)
M4:  🟢 OPEN (both directions)
Use for: Testing normal state, checking layout
```

### Scenario 2: M48 EASTBOUND CLOSED 🔴
```
M48 East: 🔴 CLOSED (high winds)
M48 West: 🟢 OPEN
M4:       🟢 OPEN (both directions)
Use for: Testing single direction closure, color coding
```

### Scenario 3: M4 WESTBOUND RESTRICTED 🟡
```
M48: 🟢 OPEN (both directions)
M4 West: 🟡 RESTRICTED (lane closure)
M4 East: 🟢 OPEN
Use for: Testing yellow status, lane vs full closure
```

### Scenario 4: BOTH BRIDGES CLOSED 🔴🔴
```
M48: 🔴 CLOSED (both directions) - emergency repairs
M4:  🔴 CLOSED (both directions) - incident
Use for: Testing worst case, multiple closures, emergency UI
```

### Scenario 5: COUNTDOWN 2 MINUTES ⏱️
```
M4 East: 🟢 OPEN with planned closure in 2 minutes
Use for: Testing countdown timer accuracy
Watch: Timer counts down every second: 2m → 1m 30s → 1m → 30s → 10s...
```

### Scenario 6: COUNTDOWN 30 MINUTES ⏰
```
M48 West: 🟢 OPEN with planned closure in 30 minutes
Use for: Testing longer countdowns, "in 30m" format
```

### Scenario 7: MULTIPLE CLOSURES 📊
```
M4 East:  🟡 RESTRICTED (active lane closure)
M48 West: 🟢 OPEN (planned closure tonight)
Use for: Testing Present Pain vs Future Pain tabs
```

### Scenario 8: FUTURE WORKS ONLY 📅
```
All: 🟢 OPEN now
M4:  Planned closure in 8 hours
M48: Planned closure tomorrow
Use for: Testing Future Pain tab, time formatting
```

## 🎨 Testing Checklist Per Scenario

- [ ] Colors correct (🟢 green, 🟡 yellow, 🔴 red)
- [ ] Directions labeled (Wales→England, England→Wales)
- [ ] Countdown timer updates (if applicable)
- [ ] Tab switching works (Present Pain / Future Pain)
- [ ] Dark mode looks good
- [ ] Text is readable
- [ ] Closure details display correctly

## 🔄 Exit Debug Mode

Long-press title → "Exit Debug Mode" → Returns to real API data

## 💡 Pro Tips

**Best for Visual Testing:**
- Scenario 5 (COUNTDOWN 2 MINUTES) - See timer in action
- Scenario 4 (BOTH BRIDGES CLOSED) - Test emergency colors

**Best for Screenshots:**
- Scenario 1 (ALL OPEN) - Normal state
- Scenario 7 (MULTIPLE CLOSURES) - Complex state

**Best for Dark Mode:**
- Try all scenarios after enabling dark mode in device settings
- Check text contrast and background colors

**Best for Demos:**
- Start with Scenario 1 (all open)
- Switch to Scenario 5 (countdown) to show timer
- Switch to Scenario 4 (all closed) to show emergency state

## 🐛 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| Menu doesn't appear | **Long-press** (hold 1-2 sec), don't tap |
| Timer not updating | Only works in COUNTDOWN scenarios |
| Colors look wrong | Check dark mode setting |
| App crashes | Rebuild: `./gradlew clean build` |

## 📱 Testing Different Conditions

### Dark Mode Test
1. Enable dark mode in device settings
2. Open app
3. Long-press title
4. Try each scenario
5. Verify: Text readable, colors don't clash

### Rotation Test  
1. Select a scenario
2. Rotate device to landscape
3. Verify: Layout adapts correctly

### Tab Switching Test
1. Select Scenario 7 (MULTIPLE CLOSURES)
2. Switch between Present Pain / Future Pain
3. Verify: Content updates, tabs animate correctly

### Countdown Accuracy Test
1. Select Scenario 5 (COUNTDOWN 2 MINUTES)
2. Note the time shown
3. Wait 30 seconds
4. Verify: Time decreased by 30 seconds
5. Watch it count down to zero

## 📊 Test Data Details

| Scenario | Active Closures | Planned Closures | Timing |
|----------|----------------|------------------|--------|
| 1. ALL OPEN | 0 | 0 | N/A |
| 2. M48 EAST CLOSED | 1 (M48 East) | 0 | Active now, ends in 2h |
| 3. M4 WEST RESTRICTED | 1 (M4 West) | 0 | Active now, ends in 3h |
| 4. BOTH CLOSED | 2 (all directions) | 0 | Both active now |
| 5. COUNTDOWN 2MIN | 0 | 1 (M4 East) | Starts in 2 minutes |
| 6. COUNTDOWN 30MIN | 0 | 1 (M48 West) | Starts in 30 minutes |
| 7. MULTIPLE | 1 (M4 East) | 1 (M48 West) | Mixed timing |
| 8. FUTURE ONLY | 0 | 2 (both bridges) | 8h and 24h away |

## 🎯 Recommended Test Order

**First Time:**
1. ALL OPEN (baseline)
2. COUNTDOWN 2 MINUTES (see timer work)
3. M48 EASTBOUND CLOSED (test single direction)

**Comprehensive:**
1. ALL OPEN → verify green state
2. M48 EASTBOUND CLOSED → verify red state one direction
3. M4 WESTBOUND RESTRICTED → verify yellow state
4. BOTH BRIDGES CLOSED → verify all red
5. COUNTDOWN 2 MINUTES → verify timer works
6. MULTIPLE CLOSURES → verify tab switching
7. FUTURE WORKS ONLY → verify Future Pain tab

**Demo Mode:**
1. Start with ALL OPEN
2. Show COUNTDOWN 2 MINUTES (people love timers!)
3. Show BOTH BRIDGES CLOSED (worst case)
4. Exit debug mode to show real data

---

**🚀 Quick Start:** Long-press title → Select "COUNTDOWN 2 MINUTES" → Watch timer count down

**📖 Full Guide:** See [TESTING_GUIDE.md](TESTING_GUIDE.md) for complete documentation
