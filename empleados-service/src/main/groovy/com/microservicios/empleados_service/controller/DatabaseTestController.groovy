package com.microservicios.empleados_service.controller

import com.microservicios.empleados_service.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.dao.DataAccessException
import org.springframework.data.mongodb.core.MongoTemplate
import org.bson.Document

@RestController
@RequestMapping("/test-db")
class DatabaseTestController {

    @Autowired
    UsuarioRepository usuarioRepository
    
    @Autowired
    MongoTemplate mongoTemplate

    @GetMapping("/ping")
    ResponseEntity<Map<String, Object>> pingDatabase() {
        Map<String, Object> response = [
            message: "Conexión a base de datos activa",
            service: "empleados-service",
            timestamp: new Date()
        ]
        return ResponseEntity.ok(response)
    }

    @GetMapping("/connection-status")
    ResponseEntity<Map<String, Object>> checkConnectionStatus() {
        try {
            // Intentar una operación simple en la base de datos
            long count = usuarioRepository.count()
            
            Map<String, Object> response = [
                connected: true,
                message: "Conexión exitosa a MongoDB",
                collection: "usuarios",
                documentCount: count,
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.ok(response)
            
        } catch (DataAccessException e) {
            Map<String, Object> response = [
                connected: false,
                message: "Error de conexión a MongoDB",
                error: e.getMessage(),
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.status(500).body(response)
        } catch (Exception e) {
            Map<String, Object> response = [
                connected: false,
                message: "Error inesperado",
                error: e.getMessage(),
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.status(500).body(response)
        }
    }

    @GetMapping("/collections-info")
    ResponseEntity<Map<String, Object>> getCollectionsInfo() {
        try {
            // Obtener información de todas las colecciones
            def collections = mongoTemplate.getCollectionNames()
            Map<String, Object> collectionsInfo = [:]
            
            collections.each { collectionName ->
                def collection = mongoTemplate.getCollection(collectionName)
                collectionsInfo[collectionName] = [
                    documentCount: collection.estimatedDocumentCount(),
                    namespace: collection.namespace.fullName
                ]
            }
            
            Map<String, Object> response = [
                connected: true,
                message: "Información de colecciones obtenida",
                collections: collectionsInfo,
                totalCollections: collections.size(),
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.ok(response)
            
        } catch (Exception e) {
            Map<String, Object> response = [
                connected: false,
                message: "Error al obtener información de colecciones",
                error: e.getMessage(),
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.status(500).body(response)
        }
    }

    @PostMapping("/test-insert")
    ResponseEntity<Map<String, Object>> testInsert(@RequestBody(required = false) Map<String, Object> testData) {
        try {
            // Crear un documento de prueba
            def testDocument = testData ?: [
                testId: "test_" + System.currentTimeMillis(),
                testName: "Prueba de conexión",
                createdAt: new Date(),
                service: "empleados-service"
            ]
            
            // Insertar directamente usando MongoTemplate
            def collection = mongoTemplate.getCollection("test_collection")
            def document = new Document(testDocument)
            collection.insertOne(document)
            
            Map<String, Object> response = [
                success: true,
                message: "Documento de prueba insertado correctamente",
                insertedId: document.getObjectId("_id").toString(),
                testData: testDocument,
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.ok(response)
            
        } catch (Exception e) {
            Map<String, Object> response = [
                success: false,
                message: "Error al insertar documento de prueba",
                error: e.getMessage(),
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.status(500).body(response)
        }
    }

    @DeleteMapping("/cleanup-test-data")
    ResponseEntity<Map<String, Object>> cleanupTestData() {
        try {
            def collection = mongoTemplate.getCollection("test_collection")
            def result = collection.deleteMany(new Document())
            
            Map<String, Object> response = [
                success: true,
                message: "Datos de prueba eliminados",
                deletedCount: result.deletedCount,
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.ok(response)
            
        } catch (Exception e) {
            Map<String, Object> response = [
                success: false,
                message: "Error al eliminar datos de prueba",
                error: e.getMessage(),
                service: "empleados-service",
                timestamp: new Date()
            ]
            return ResponseEntity.status(500).body(response)
        }
    }
}