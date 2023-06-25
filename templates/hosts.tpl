[production]
Prod1   ansible_host=${public_ips[0]}
Prod2   ansible_host=${public_ips[1]}

[agent]
J_Agent ansible_host=${public_ips[2]}

[controller]
J_Conroller ansible_host=${public_ips[3]}