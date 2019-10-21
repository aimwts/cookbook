import time
from swimai import SwimClient
from swimai import Text


async def my_custom_did_set_async(new_value, old_value):
    print(f'link watched info change to {new_value} from {old_value}')


if __name__ == '__main__':
    swim_client = SwimClient()
    swim_client.start()

    host_uri = 'ws://localhost:9001'
    node_uri = '/unit/foo'
    lane_uri = 'info'

    link = swim_client.downlink_value().set_host_uri(host_uri).set_node_uri(node_uri).set_lane_uri(lane_uri).did_set(my_custom_did_set_async).open()
    link.set(Text.create_from('Hello from Python'))

    time.sleep(2)
    print("Will shut down client in 2 seconds")
    time.sleep(2)
    swim_client.stop()
