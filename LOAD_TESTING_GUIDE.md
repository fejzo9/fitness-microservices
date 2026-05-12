# Load Testing Guide for Workout Service

## Overview
This guide explains how to test load balancing between two workout service instances and measure performance differences.

## Setup

### 1. Start Both Instances
```bash
# Start Eureka and both workout service instances
docker-compose up eureka-service workout-service workout-service-2 --build
```

### 2. Verify Instances are Running
Check Eureka UI: http://localhost:8761
You should see two instances of "WORKOUT-SERVICE" registered.

### 3. Test Individual Instances
```bash
# Test instance 1
curl http://localhost:8083/api/load-test/health

# Test instance 2  
curl http://localhost:8086/api/load-test/health
```

## Running Load Tests

### Simple Load Test (Recommended)
```bash
# Install Python requests if needed
pip install requests

# Run the simple load test
python simple_load_test.py
```

This script will:
- Send 100 requests total (50 to each instance in round-robin)
- Measure response times for each request
- Track which instance handled each request
- Generate detailed statistics and save results to JSON

### Advanced Load Test
```bash
# Run the comprehensive load test
python load_test.py
```

This script includes:
- Direct instance testing (no load balancing)
- Load balanced testing simulation
- Performance comparison
- Detailed statistical analysis

## Understanding the Results

### Key Metrics

1. **Response Time Statistics**
   - Average response time across all requests
   - Median response time (50th percentile)
   - Min/Max response times
   - Standard deviation (consistency measure)

2. **Instance Distribution**
   - How many requests each instance handled
   - Percentage distribution
   - Load balancing effectiveness

3. **Success Rate**
   - Percentage of successful requests
   - Failed request analysis

### Expected Results

With proper load balancing:
- Each instance should handle ~50% of requests
- Response times should be similar between instances
- Overall success rate should be 95%+

### Performance Comparison

**Without Load Balancing** (single instance):
- All requests go to one instance
- Higher load on single server
- Potentially higher response times under load

**With Load Balancing** (multiple instances):
- Requests distributed across instances
- Reduced load per instance
- Better resource utilization
- Improved response times under load

## Files Generated

- `load_test_results.json` - Detailed results from simple test
- `direct_instances_results.json` - Direct instance test results
- `load_balanced_results.json` - Load balanced test results

## Troubleshooting

### Common Issues

1. **Port Conflicts**
   - Ensure ports 8083 and 8086 are available
   - Check for other services using these ports

2. **Connection Refused**
   - Verify both instances are running
   - Check Docker containers: `docker-compose ps`

3. **Uneven Distribution**
   - Check Eureka registration
   - Verify instance IDs are unique

4. **High Response Times**
   - Check system resources
   - Verify database connectivity
   - Check application logs

### Monitoring

Check container logs:
```bash
# Instance 1 logs
docker-compose logs workout-service

# Instance 2 logs  
docker-compose logs workout-service-2

# Eureka logs
docker-compose logs eureka-service
```

## Customization

### Modify Test Parameters

Edit `simple_load_test.py` to change:
- Number of requests (change `range(100)`)
- Delay between requests (change `time.sleep(0.01)`)
- Load balancing strategy (change the alternating logic)

### Add More Complex Tests

For more realistic testing:
1. Add different request types
2. Include POST requests
3. Test with different payload sizes
4. Add concurrent requests using threading

## Performance Analysis

Use the generated JSON files to:
- Create charts and graphs
- Analyze trends over time
- Compare different configurations
- Identify performance bottlenecks

Example analysis with Python:
```python
import json
import matplotlib.pyplot as plt

# Load results
with open('load_test_results.json') as f:
    data = json.load(f)

# Analyze response times
response_times = [r['response_time_ms'] for r in data['detailed_results'] if r['success']]
plt.hist(response_times, bins=20)
plt.xlabel('Response Time (ms)')
plt.ylabel('Frequency')
plt.title('Response Time Distribution')
plt.show()
```
