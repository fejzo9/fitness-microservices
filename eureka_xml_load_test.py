#!/usr/bin/env python3
import requests
import time
import statistics
from datetime import datetime
from collections import defaultdict
import xml.etree.ElementTree as ET
import random

class EurekaXmlLoadTester:
    def __init__(self, eureka_url="http://localhost:8761"):
        self.eureka_url = eureka_url
        self.results = []
        self.instance_counter = 0
        
    def get_service_instances(self, service_name="WORKOUT-SERVICE"):
        """Get registered instances from Eureka (XML parsing)"""
        try:
            # Try direct service endpoint first
            response = requests.get(f"{self.eureka_url}/eureka/apps/{service_name}", timeout=5)
            if response.status_code == 200:
                instances = []
                
                # Parse XML response
                try:
                    root = ET.fromstring(response.text)
                    
                    # Find all instance elements
                    for instance_elem in root.findall('.//instance'):
                        # Extract port
                        port_elem = instance_elem.find('port')
                        if port_elem is not None:
                            port_text = port_elem.get('enabled', '8081')
                            port = int(port_text) if port_text.isdigit() else 8081
                        else:
                            port = 8081
                        
                        # Extract IP address
                        ip_addr_elem = instance_elem.find('ipAddr')
                        ip_addr = ip_addr_elem.text if ip_addr_elem is not None else 'localhost'
                        
                        # Extract instance ID
                        instance_id_elem = instance_elem.find('instanceId')
                        instance_id = instance_id_elem.text if instance_id_elem is not None else 'unknown'
                        
                        # Extract hostname
                        host_name_elem = instance_elem.find('hostName')
                        host_name = host_name_elem.text if host_name_elem is not None else ip_addr
                        
                        # Extract status
                        status_elem = instance_elem.find('status')
                        status = status_elem.text if status_elem is not None else 'UNKNOWN'
                        
                        # Only include UP instances
                        if status.upper() == 'UP':
                            # Map internal instances to external ports
                            # Instance 1 (fcc284a834a343663d89f03c66f4d9ca) -> port 8083
                            # Instance 2 (c205d81b99276110883802f3792adb28) -> port 8086
                            if 'fcc284a834a343663d89f03c66f4d9ca' in instance_id:
                                external_port = 8083
                            elif 'c205d81b99276110883802f3792adb28' in instance_id:
                                external_port = 8086
                            else:
                                external_port = port  # fallback to internal port
                            
                            # Use localhost with mapped port instead of internal IP
                            instance_url = f"http://localhost:{external_port}"
                            instances.append({
                                'url': instance_url,
                                'instance_id': instance_id,
                                'port': external_port,
                                'ip': ip_addr,
                                'hostname': host_name,
                                'status': status,
                                'internal_port': port
                            })
                    
                    print(f"✅ Found {len(instances)} UP instances for {service_name}")
                    return instances
                    
                except ET.ParseError as e:
                    print(f"❌ XML parsing error: {e}")
                    print(f"Response text: {response.text[:300]}...")
                    return []
            else:
                print(f"❌ Failed to get instances from Eureka: {response.status_code}")
                return []
        except Exception as e:
            print(f"❌ Error connecting to Eureka: {e}")
            return []
    
    def send_request_to_instance(self, instance):
        """Send request to a specific instance"""
        try:
            start_time = time.time()
            response = requests.get(f"{instance['url']}/api/load-test", timeout=10)
            end_time = time.time()
            
            response_time = (end_time - start_time) * 1000  # Convert to milliseconds
            
            # Parse response to get instance info
            response_instance_id = "unknown"
            if response.status_code == 200:
                try:
                    data = response.json()
                    response_instance_id = data.get('instanceId', 'unknown')
                except:
                    pass
            
            result = {
                'url': instance['url'],
                'eureka_instance_id': instance['instance_id'],
                'response_instance_id': response_instance_id,
                'response_time_ms': response_time,
                'status_code': response.status_code,
                'timestamp': datetime.now().isoformat(),
                'success': response.status_code == 200
            }
            
            return result
        except Exception as e:
            return {
                'url': instance['url'],
                'eureka_instance_id': instance['instance_id'],
                'response_instance_id': 'error',
                'response_time_ms': -1,
                'status_code': 0,
                'timestamp': datetime.now().isoformat(),
                'success': False,
                'error': str(e)
            }
    
    def round_robin_load_balance(self, instances):
        """Simple round-robin load balancing"""
        if not instances:
            return None
        
        instance = instances[self.instance_counter % len(instances)]
        self.instance_counter += 1
        return instance
    
    def random_load_balance(self, instances):
        """Random load balancing"""
        if not instances:
            return None
        return random.choice(instances)
    
    def test_eureka_load_balancing(self, num_requests=100, strategy="round-robin"):
        """Test load balancing using Eureka service discovery"""
        print(f"=== Eureka-Based Load Balancing Test ({num_requests} requests) ===")
        print(f"Strategy: {strategy}")
        
        # Get instances from Eureka
        print("Discovering service instances from Eureka...")
        instances = self.get_service_instances()
        
        if not instances:
            print("❌ No UP instances found in Eureka registry!")
            print("Make sure workout-service instances are running and registered with Eureka.")
            return []
        
        print(f"✅ Found {len(instances)} instances:")
        for i, instance in enumerate(instances):
            print(f"  {i+1}. {instance['instance_id']} at {instance['url']} (Status: {instance['status']})")
        
        print(f"\nStarting load test with {strategy} strategy...")
        
        results = []
        instance_counts = defaultdict(int)
        
        for i in range(num_requests):
            # Select instance based on strategy
            if strategy == "round-robin":
                instance = self.round_robin_load_balance(instances)
            elif strategy == "random":
                instance = self.random_load_balance(instances)
            else:
                instance = self.round_robin_load_balance(instances)
            
            if instance:
                result = self.send_request_to_instance(instance)
                result['request_number'] = i + 1
                result['strategy'] = strategy
                results.append(result)
                
                if result['success']:
                    instance_counts[result['eureka_instance_id']] += 1
                
                # Progress indicator
                if (i + 1) % 20 == 0:
                    print(f"  Sent {i + 1} requests...")
                
                # Small delay between requests
                time.sleep(0.01)
            else:
                print(f"❌ No instance available for request {i + 1}")
        
        # Calculate instance distribution from results
        instance_counts = defaultdict(int)
        for result in results:
            if result['success']:
                instance_counts[result['eureka_instance_id']] += 1
        
        # Analyze and display results
        self.analyze_results(results, strategy, instance_counts)
        
        return results
    
    def analyze_results(self, results, strategy, instance_counts):
        """Analyze and display test results"""
        successful_results = [r for r in results if r['success']]
        failed_results = [r for r in results if not r['success']]
        
        print(f"\n=== RESULTS ({strategy.upper()} STRATEGY) ===")
        print(f"Total requests: {len(results)}")
        print(f"Successful: {len(successful_results)}")
        print(f"Failed: {len(failed_results)}")
        print(f"Success rate: {(len(successful_results)/len(results))*100:.1f}%")
        
        if successful_results:
            response_times = [r['response_time_ms'] for r in successful_results]
            
            print(f"\nResponse Time Statistics (ms):")
            print(f"  Average: {statistics.mean(response_times):.2f}")
            print(f"  Median: {statistics.median(response_times):.2f}")
            print(f"  Min: {min(response_times):.2f}")
            print(f"  Max: {max(response_times):.2f}")
            print(f"  Std Dev: {statistics.stdev(response_times):.2f}")
            
            print(f"\nInstance Distribution (from Eureka):")
            for instance_id, count in instance_counts.items():
                percentage = (count / len(successful_results)) * 100
                print(f"  {instance_id}: {count} requests ({percentage:.1f}%)")
        
        if failed_results:
            print(f"\nFailed Requests:")
            for i, failed in enumerate(failed_results[:5]):  # Show first 5 failures
                print(f"  {failed['request_number']}. {failed.get('error', 'Unknown error')}")
            if len(failed_results) > 5:
                print(f"  ... and {len(failed_results) - 5} more failures")
        
        # Save results
        filename = f'eureka_xml_load_test_{strategy.replace("-", "_")}_results.json'
        import json
        with open(filename, 'w') as f:
            json.dump({
                'summary': {
                    'total_requests': len(results),
                    'successful': len(successful_results),
                    'failed': len(failed_results),
                    'success_rate': (len(successful_results)/len(results))*100 if results else 0,
                    'strategy': strategy,
                    'instance_distribution': dict(instance_counts)
                },
                'detailed_results': results
            }, f, indent=2)
        
        print(f"\nDetailed results saved to: {filename}")

def main():
    print("Eureka-Based Load Testing for Workout Service (XML Parsing)")
    print("=" * 60)
    
    # Check Eureka connectivity
    print("Testing Eureka connectivity...")
    try:
        response = requests.get("http://localhost:8761/eureka/apps", timeout=5)
        if response.status_code == 200:
            print("✅ Eureka server is accessible")
        else:
            print(f"❌ Eureka returned status {response.status_code}")
            return
    except Exception as e:
        print(f"❌ Cannot connect to Eureka: {e}")
        return
    
    tester = EurekaXmlLoadTester()
    
    print("\nStarting Eureka-based load testing...")
    input("Press Enter to continue...")
    
    # Test different load balancing strategies
    strategies = ["round-robin", "random"]
    
    for strategy in strategies:
        print(f"\n{'='*60}")
        print(f"Testing {strategy.upper()} strategy")
        print(f"{'='*60}")
        
        results = tester.test_eureka_load_balancing(100, strategy)
        
        if strategy != strategies[-1]:
            input(f"\nPress Enter to test {strategies[strategies.index(strategy) + 1]} strategy...")
    
    print(f"\n{'='*60}")
    print("All Eureka-based load tests completed!")
    print(f"{'='*60}")

if __name__ == "__main__":
    main()
