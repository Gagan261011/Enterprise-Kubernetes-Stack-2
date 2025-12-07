apiVersion: kubeadm.k8s.io/v1beta3
kind: InitConfiguration
localAPIEndpoint:
  advertiseAddress: ${master_private_ip}
  bindPort: 6443
nodeRegistration:
  criSocket: unix:///var/run/containerd/containerd.sock
  name: k8s-master-1
  taints:
    - effect: NoSchedule
      key: node-role.kubernetes.io/control-plane
---
apiVersion: kubeadm.k8s.io/v1beta3
kind: ClusterConfiguration
clusterName: ${cluster_name}
kubernetesVersion: v1.29.0
controlPlaneEndpoint: "${master_public_ip}:6443"
networking:
  podSubnet: ${pod_network_cidr}
  serviceSubnet: ${service_cidr}
apiServer:
  certSANs:
    - "${master_public_ip}"
    - "${master_private_ip}"
    - "localhost"
    - "127.0.0.1"
  extraArgs:
    authorization-mode: "Node,RBAC"
controllerManager:
  extraArgs:
    bind-address: "0.0.0.0"
scheduler:
  extraArgs:
    bind-address: "0.0.0.0"
---
apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
cgroupDriver: systemd
