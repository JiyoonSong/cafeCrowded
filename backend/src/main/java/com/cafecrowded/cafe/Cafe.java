package com.cafecrowded.cafe;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cafe")
public class Cafe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "external_place_id", nullable = false, updatable = false)
	private String externalPlaceId;

	@Column(nullable = false)
	private String name;

	private String address;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;

	// Set by database defaults.
	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Instant updatedAt;

	protected Cafe() {
	}

	public Cafe(String externalPlaceId, String name, String address, double latitude, double longitude) {
		this.externalPlaceId = externalPlaceId;
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Long getId() { return id; }
	public String getExternalPlaceId() { return externalPlaceId; }
	public String getName() { return name; }
	public String getAddress() { return address; }
	public double getLatitude() { return latitude; }
	public double getLongitude() { return longitude; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
