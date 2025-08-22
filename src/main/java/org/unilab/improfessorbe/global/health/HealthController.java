package org.unilab.improfessorbe.global.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	@Autowired
	private HealthEndpoint healthEndpoint;

	@GetMapping("/")
	public ResponseEntity<?> healthCheck() {
		// /actuator/health와 동일한 응답 반환
		return ResponseEntity.ok(healthEndpoint.health());
	}

}
