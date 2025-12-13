# PIXEL-V2 NAS (Network Attached Storage)

## Overview

The PIXEL-V2 NAS service provides shared network storage using Samba (SMB/CIFS protocol) for file sharing across the infrastructure.

## Service Details

- **Container Name**: `pixel-v2-nas`
- **Image**: `dperson/samba:latest`
- **Ports**:
  - `445` (SMB)
  - `139` (NetBIOS)
- **Workgroup**: `PIXEL`

## Shared Folders

The NAS provides two shared folders:

### 1. Shared Storage (`/shared`)

- **Purpose**: General shared storage for application files, configurations, and temporary data
- **Access**: Read/Write for authenticated users
- **Mount Point**: `/shared` inside container
- **Volume**: `pixel-v2-nas-shared`

### 2. Data Storage (`/data`)

- **Purpose**: Persistent data storage for application data, logs, and backups
- **Access**: Read/Write for authenticated users
- **Mount Point**: `/data` inside container
- **Volume**: `pixel-v2-nas-data`

## Authentication

- **Username**: `pixel`
- **Password**: `pixel`
- **User ID**: `1000`
- **Group ID**: `1000`

## Accessing the NAS

### From Docker Containers

Other containers in the `pixel-v2-network` can access the NAS using:

```bash
# Mount shared folder
mount -t cifs //pixel-v2-nas/shared /mnt/shared -o username=pixel,password=pixel

# Mount data folder
mount -t cifs //pixel-v2-nas/data /mnt/data -o username=pixel,password=pixel
```

### From Host System (macOS/Linux)

```bash
# Create mount points
sudo mkdir -p /mnt/pixel-shared /mnt/pixel-data

# Mount shared folder
sudo mount -t cifs //localhost/shared /mnt/pixel-shared -o username=pixel,password=pixel

# Mount data folder
sudo mount -t cifs //localhost/data /mnt/pixel-data -o username=pixel,password=pixel
```

### From Windows

1. Open File Explorer
2. In the address bar, type: `\\localhost\shared` or `\\localhost\data`
3. Enter credentials:
   - Username: `pixel`
   - Password: `pixel`

### From Finder (macOS)

1. Open Finder
2. Press `Cmd+K` (Connect to Server)
3. Enter: `smb://localhost/shared` or `smb://localhost/data`
4. Enter credentials when prompted

## Docker Compose Commands

### Start NAS service

```bash
docker-compose up -d nas
```

### Stop NAS service

```bash
docker-compose stop nas
```

### View NAS logs

```bash
docker-compose logs -f nas
```

### Restart NAS service

```bash
docker-compose restart nas
```

## Use Cases

### Application File Sharing

- Share configuration files between multiple application instances
- Exchange processed files between different services
- Temporary storage for file transformations

### Log Aggregation

- Centralize logs from multiple containers
- Backup and archive log files
- Share logs with monitoring tools

### Data Exchange

- Import/export data files
- Share reference data between applications
- Backup and restore operations

### Development

- Share source code and configurations during development
- Exchange test data and fixtures
- Collaborative file editing

## Integration Examples

### Mount in Application Container

Add to your application service in docker-compose.yml:

```yaml
services:
  your-app:
    # ... other configuration
    volumes:
      - type: volume
        source: nas_shared
        target: /app/shared
      - type: volume
        source: nas_data
        target: /app/data
    depends_on:
      - nas
```

### Access from Application Code

```java
// Java example - reading shared configuration
Path configPath = Paths.get("/app/shared/config.properties");
Properties config = new Properties();
config.load(Files.newInputStream(configPath));

// Writing processed data
Path outputPath = Paths.get("/app/data/processed-" + timestamp + ".json");
Files.write(outputPath, jsonData.getBytes());
```

## Security Notes

- The NAS uses basic authentication (username/password)
- Default credentials should be changed in production environments
- Network access is restricted to the `pixel-v2-network`
- Consider using encrypted volumes for sensitive data

## Troubleshooting

### Connection Issues

```bash
# Test NAS connectivity
docker exec -it pixel-v2-nas smbclient -L localhost -U pixel

# Check if ports are accessible
telnet localhost 445
telnet localhost 139
```

### Permission Issues

```bash
# Check user/group IDs in container
docker exec -it pixel-v2-nas id pixel

# Fix ownership if needed
docker exec -it pixel-v2-nas chown -R pixel:pixel /shared /data
```

### View Samba Configuration

```bash
# Show current Samba configuration
docker exec -it pixel-v2-nas cat /etc/samba/smb.conf
```
