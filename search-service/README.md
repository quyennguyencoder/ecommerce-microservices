docker exec -it elasticsearch bash
curl -u elastic:javabuilder -X POST "http://localhost:9200/_security/user/kibana_system/_password" -H "Content-Type: application/json" -d '{"password":"javabuilder"}'
dlhKbXZad0JnbzZORVJRc3NzdnE6Z0tCbXBvZ01JdmlCRkU2ZjA0V3YyZw==