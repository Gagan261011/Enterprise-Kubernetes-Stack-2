[masters]
%{ for idx, ip in master_public_ips ~}
master-${idx + 1} ansible_host=${ip} ansible_user=${ssh_user} private_ip=${master_private_ips[idx]}
%{ endfor ~}

[workers]
%{ for idx, ip in worker_public_ips ~}
worker-${idx + 1} ansible_host=${ip} ansible_user=${ssh_user} private_ip=${worker_private_ips[idx]}
%{ endfor ~}

[all:vars]
ansible_ssh_private_key_file=${ssh_key_path}
ansible_ssh_common_args='-o StrictHostKeyChecking=no'
