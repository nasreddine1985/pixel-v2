# k-techpivot (Generic TechPivot Manipulator)

This ZIP contains:
- `kamelets/k-techpivot.yaml` : the generic Kamelet (ensure/put/add/merge/get)
- `java/com/pixelv2/techpivot/PivotService.java` : required bean implementation
- `java/com/pixelv2/techpivot/PivotConfiguration.java` : Spring Boot bean declaration

## Usage (examples)

### Ensure pivot exists
`toD: "kamelet:k-techpivot?op=ensure&flowCode={{flowCode}}"`

### Put a value
`toD: "kamelet:k-techpivot?op=put&path=Flow.FlowCode&value={{flowCode}}&valueType=string"`

### Add to list
`toD: "kamelet:k-techpivot?op=add&path=Output&json={\"PartnerCode\":\"OUT1\"}"`

### Merge
`toD: "kamelet:k-techpivot?op=merge&path=Flow&json={\"FlowEnabled\":\"true\"}"`

### Get full pivot snapshot as JSON into header
`toD: "kamelet:k-techpivot?op=get&targetHeader=TechPivotJson"`
