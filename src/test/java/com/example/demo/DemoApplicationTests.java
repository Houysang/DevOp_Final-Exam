package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.TemplateRepository;
import com.example.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DemoApplicationTests {

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private QrCodeService qrCodeService;

	@Autowired
	private FileStorageService fileStorageService;

	private Profile testProfile;

	@BeforeEach
	void setUp() {
		profileRepository.deleteAll();
		templateRepository.deleteAll();

		// Create a test profile using ProfileBuilder
		testProfile = new ProfileBuilder()
				.withDefaults(ProfileType.STUDENT)
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@example.com")
				.phoneNumber("0123456789")
				.department("Computer Science")
				.position("Student")
				.dateOfBirth(LocalDate.of(2000, 1, 15))
				.address("123 Main Street")
				.build();
	}

	// ========== MODEL TESTS ==========

	@Test
	void testProfileTypeEnum() {
		assertEquals(ProfileType.STUDENT, ProfileType.valueOf("STUDENT"));
		assertEquals(ProfileType.EMPLOYEE, ProfileType.valueOf("EMPLOYEE"));
		assertEquals(ProfileType.USER, ProfileType.valueOf("USER"));
		assertEquals(3, ProfileType.values().length);
	}

	@Test
	void testBarcodeTypeEnum() {
		assertEquals(BarcodeType.CODE_128, BarcodeType.valueOf("CODE_128"));
		assertEquals(BarcodeType.EAN_13, BarcodeType.valueOf("EAN_13"));
		assertEquals(2, BarcodeType.values().length);
	}

	@Test
	void testProfileBuilder() {
		assertNotNull(testProfile);
		assertEquals("John", testProfile.getFirstName());
		assertEquals("Doe", testProfile.getLastName());
		assertEquals("John Doe", testProfile.getFullName());
		assertEquals(ProfileType.STUDENT, testProfile.getProfileType());
		assertNotNull(testProfile.getUniqueId());
		assertTrue(testProfile.getUniqueId().startsWith("STD-2026-"));
		assertEquals("john.doe@example.com", testProfile.getEmail());
		assertEquals("Computer Science", testProfile.getDepartment());
	}

	@Test
	void testUniqueIdGeneration() {
		String uuidId = ProfileBuilder.generateUniqueId(ProfileType.STUDENT);
		assertTrue(uuidId.startsWith("STD-"));

		String empId = ProfileBuilder.generateUniqueId(ProfileType.EMPLOYEE);
		assertTrue(empId.startsWith("EMP-"));

		String userId = ProfileBuilder.generateUniqueId(ProfileType.USER);
		assertTrue(userId.startsWith("USR-"));

		// Test sequence ID
		String seqId = ProfileBuilder.generateSequenceId(ProfileType.STUDENT, "Computer Science", 1);
		assertTrue(seqId.contains("COM-001"));
	}

	// ========== REPOSITORY TESTS ==========

	@Test
	void testSaveAndFindProfile() {
		Profile saved = profileRepository.save(testProfile);
		assertNotNull(saved.getId());

		Profile found = profileRepository.findById(saved.getId()).orElse(null);
		assertNotNull(found);
		assertEquals("John", found.getFirstName());
	}

	@Test
	void testFindProfileByUniqueId() {
		Profile saved = profileRepository.save(testProfile);
		Profile found = profileRepository.findByUniqueId(saved.getUniqueId()).orElse(null);
		assertNotNull(found);
		assertEquals(saved.getId(), found.getId());
	}

	@Test
	void testFindProfilesByType() {
		profileRepository.save(testProfile);
		Profile empProfile = Profile.builder()
				.uniqueId(ProfileBuilder.generateUniqueId(ProfileType.EMPLOYEE))
				.firstName("Jane")
				.lastName("Smith")
				.profileType(ProfileType.EMPLOYEE)
				.build();
		profileRepository.save(empProfile);

		List<Profile> students = profileRepository.findByProfileType(ProfileType.STUDENT);
		assertEquals(1, students.size());

		List<Profile> employees = profileRepository.findByProfileType(ProfileType.EMPLOYEE);
		assertEquals(1, employees.size());
	}

	@Test
	void testSearchProfiles() {
		profileRepository.save(testProfile);

		List<Profile> results = profileRepository.searchProfiles("John");
		assertFalse(results.isEmpty());

		results = profileRepository.searchProfiles("Doe");
		assertFalse(results.isEmpty());

		results = profileRepository.searchProfiles("Computer Science");
		assertFalse(results.isEmpty());
	}

	@Test
	void testExistsByUniqueId() {
		Profile saved = profileRepository.save(testProfile);
		assertTrue(profileRepository.existsByUniqueId(saved.getUniqueId()));
		assertFalse(profileRepository.existsByUniqueId("NONEXISTENT-ID"));
	}

	@Test
	void testDeleteProfile() {
		Profile saved = profileRepository.save(testProfile);
		assertTrue(profileRepository.existsById(saved.getId()));
		profileRepository.deleteById(saved.getId());
		assertFalse(profileRepository.existsById(saved.getId()));
	}

	// ========== TEMPLATE REPOSITORY TESTS ==========

	@Test
	void testSaveAndFindTemplate() {
		Template template = Template.builder()
				.name("Standard Card")
				.description("Standard ID card layout")
				.htmlContent("<div>ID Card Content</div>")
				.cssContent("body { font-family: Arial; }")
				.profileType(ProfileType.STUDENT)
				.defaultTemplate(true)
				.build();

		Template saved = templateRepository.save(template);
		assertNotNull(saved.getId());

		Template found = templateRepository.findById(saved.getId()).orElse(null);
		assertNotNull(found);
		assertEquals("Standard Card", found.getName());
	}

	@Test
	void testFindTemplateByName() {
		Template template = Template.builder()
				.name("Test Template")
				.htmlContent("<div>Content</div>")
				.build();
		templateRepository.save(template);

		Template found = templateRepository.findByName("Test Template").orElse(null);
		assertNotNull(found);
	}

	@Test
	void testFindTemplateByProfileType() {
		Template studentTemplate = Template.builder()
				.name("Student Card")
				.htmlContent("<div>Student</div>")
				.profileType(ProfileType.STUDENT)
				.build();
		templateRepository.save(studentTemplate);

		List<Template> studentTemplates = templateRepository.findByProfileType(ProfileType.STUDENT);
		assertEquals(1, studentTemplates.size());

		List<Template> employeeTemplates = templateRepository.findByProfileType(ProfileType.EMPLOYEE);
		assertTrue(employeeTemplates.isEmpty());
	}

	@Test
	void testFindDefaultTemplate() {
		Template template1 = Template.builder()
				.name("Template 1")
				.htmlContent("<div>1</div>")
				.defaultTemplate(false)
				.build();
		templateRepository.save(template1);

		Template defaultTemplate = Template.builder()
				.name("Default")
				.htmlContent("<div>Default</div>")
				.defaultTemplate(true)
				.build();
		templateRepository.save(defaultTemplate);

		Template found = templateRepository.findByDefaultTemplateTrue().orElse(null);
		assertNotNull(found);
		assertEquals("Default", found.getName());
	}

	@Test
	void testSearchTemplates() {
		Template template = Template.builder()
				.name("Searchable Template")
				.description("This is a test template for searching")
				.htmlContent("<div>Content</div>")
				.build();
		templateRepository.save(template);

		List<Template> results = templateRepository.searchTemplates("Searchable");
		assertFalse(results.isEmpty());

		results = templateRepository.searchTemplates("test template");
		assertFalse(results.isEmpty());
	}

	// ========== SERVICE TESTS ==========

	@Test
	void testProfileServiceCreate() {
		com.example.demo.dto.ProfileDTO profileDTO = com.example.demo.dto.ProfileDTO.builder()
				.firstName("Alice")
				.lastName("Johnson")
				.email("alice@example.com")
				.profileType(ProfileType.EMPLOYEE)
				.department("Engineering")
				.position("Developer")
				.build();

		Profile created = profileService.createProfile(profileDTO);
		assertNotNull(created.getId());
		assertEquals("Alice", created.getFirstName());
		assertEquals("EMPLOYEE", created.getProfileType().toString());
	}

	@Test
	void testProfileServiceUpdate() {
		Profile saved = profileRepository.save(testProfile);

		com.example.demo.dto.ProfileDTO updateDTO = com.example.demo.dto.ProfileDTO.builder()
				.firstName("Updated")
				.lastName("Name")
				.email("updated@example.com")
				.profileType(ProfileType.EMPLOYEE)
				.build();

		Profile updated = profileService.updateProfile(saved.getId(), updateDTO);
		assertEquals("Updated", updated.getFirstName());
		assertEquals("Name", updated.getLastName());
		assertEquals(ProfileType.EMPLOYEE, updated.getProfileType());
	}

	@Test
	void testProfileServiceDelete() {
		Profile saved = profileRepository.save(testProfile);
		assertNotNull(profileRepository.findById(saved.getId()));

		profileService.deleteProfile(saved.getId());
		assertTrue(profileRepository.findById(saved.getId()).isEmpty());
	}

	@Test
	void testProfileServiceGetAll() {
		profileRepository.save(testProfile);

		Profile anotherProfile = new ProfileBuilder()
				.withDefaults(ProfileType.EMPLOYEE)
				.firstName("Jane")
				.lastName("Smith")
				.build();
		profileRepository.save(anotherProfile);

		List<Profile> allProfiles = profileService.getAllProfiles();
		assertEquals(2, allProfiles.size());
	}

	@Test
	void testTemplateServiceCreate() {
		Template template = Template.builder()
				.name("Test Template")
				.htmlContent("<div>Content</div>")
				.build();

		Template created = templateService.createTemplate(template);
		assertNotNull(created.getId());
		assertEquals("Test Template", created.getName());
	}

	@Test
	void testTemplateServiceSetDefault() {
		Template template1 = Template.builder()
				.name("Template 1")
				.htmlContent("<div>1</div>")
				.defaultTemplate(false)
				.build();
		Template saved1 = templateRepository.save(template1);

		Template template2 = Template.builder()
				.name("Template 2")
				.htmlContent("<div>2</div>")
				.defaultTemplate(false)
				.build();
		Template saved2 = templateRepository.save(template2);

		templateService.setDefaultTemplate(saved1.getId());
		assertTrue(templateRepository.findById(saved1.getId()).get().isDefaultTemplate());
		assertFalse(templateRepository.findById(saved2.getId()).get().isDefaultTemplate());

		templateService.setDefaultTemplate(saved2.getId());
		assertFalse(templateRepository.findById(saved1.getId()).get().isDefaultTemplate());
		assertTrue(templateRepository.findById(saved2.getId()).get().isDefaultTemplate());
	}

	// ========== QR CODE / BARCODE SERVICE TESTS ==========

	@Test
	void testGenerateQRCode() throws Exception {
		java.awt.image.BufferedImage qrImage = qrCodeService.generateQRCode("Test QR Code Data");
		assertNotNull(qrImage);
	}

	@Test
	void testGenerateProfileQRCode() throws Exception {
		java.awt.image.BufferedImage qrImage = qrCodeService.generateProfileQRCode("TEST-123", "John Doe", "STUDENT");
		assertNotNull(qrImage);
	}

	@Test
	void testGenerateCode128Barcode() throws Exception {
		java.awt.image.BufferedImage barcodeImage = qrCodeService.generateCode128Barcode("TEST123");
		assertNotNull(barcodeImage);
	}

	@Test
	void testGenerateBarcodeByType() throws Exception {
		java.awt.image.BufferedImage code128 = qrCodeService.generateBarcode("12345", BarcodeType.CODE_128);
		assertNotNull(code128);
	}

	// ========== FILE STORAGE SERVICE TESTS ==========

	@Test
	void testFileStorage() {
		MultipartFile mockFile = new MultipartFile() {
			@Override
			public String getName() { return "test.jpg"; }
			@Override
			public String getOriginalFilename() { return "test.jpg"; }
			@Override
			public String getContentType() { return "image/jpeg"; }
			@Override
			public boolean isEmpty() { return false; }
			@Override
			public long getSize() { return 1024; }
			@Override
			public byte[] getBytes() { return "test image data".getBytes(); }
			@Override
			public InputStream getInputStream() { return new ByteArrayInputStream("test image data".getBytes()); }
			@Override
			public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
				java.nio.file.Files.write(dest.toPath(), getBytes());
			}
		};

		String filename = fileStorageService.storeFile(mockFile);
		assertNotNull(filename);
		assertTrue(filename.endsWith(".jpg"));

		byte[] loaded = fileStorageService.loadFile(filename);
		assertNotNull(loaded);

		fileStorageService.deleteFile(filename);
	}

	// ========== EDGE CASE TESTS ==========

	@Test
	void testProfileWithoutOptionalFields() {
		Profile minimalProfile = Profile.builder()
				.uniqueId(ProfileBuilder.generateUniqueId(ProfileType.USER))
				.firstName("Minimal")
				.lastName("User")
				.profileType(ProfileType.USER)
				.build();

		Profile saved = profileRepository.save(minimalProfile);
		assertNotNull(saved.getId());
		assertNull(saved.getEmail());
		assertNull(saved.getDepartment());
	}

	@Test
	void testProfileBuilderWithOnlyRequiredFields() {
		Profile profile = new ProfileBuilder()
				.withDefaults(ProfileType.EMPLOYEE)
				.firstName("Test")
				.lastName("User")
				.build();

		assertEquals("Test", profile.getFirstName());
		assertEquals("User", profile.getLastName());
		assertNotNull(profile.getUniqueId());
		assertNull(profile.getEmail());
	}

	@Test
	void testProfileCountByTypeAndDepartment() {
		// Save multiple profiles
		for (int i = 0; i < 3; i++) {
			Profile p = new ProfileBuilder()
					.withDefaults(ProfileType.STUDENT)
					.firstName("Student" + i)
					.lastName("Test")
					.department("Computer Science")
					.build();
			profileRepository.save(p);
		}

		long count = profileRepository.countByProfileTypeAndDepartment(ProfileType.STUDENT, "Computer Science");
		assertEquals(3, count);
	}

	@Test
	void testAllProfilesOrderedByCreatedAt() {
		Profile p1 = profileRepository.save(testProfile);

		Profile p2 = new ProfileBuilder()
				.withDefaults(ProfileType.EMPLOYEE)
				.firstName("Second")
				.lastName("Profile")
				.build();
		profileRepository.save(p2);

		List<Profile> profiles = profileRepository.findAllByOrderByCreatedAtDesc();
		assertEquals(2, profiles.size());
	}
}