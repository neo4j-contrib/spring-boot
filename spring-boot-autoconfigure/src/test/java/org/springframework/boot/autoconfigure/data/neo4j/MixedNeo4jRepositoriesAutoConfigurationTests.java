/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.data.neo4j;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.TestAutoConfigurationPackage;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.city.City;
import org.springframework.boot.autoconfigure.data.jpa.city.CityRepository;
import org.springframework.boot.autoconfigure.data.neo4j.country.Country;
import org.springframework.boot.autoconfigure.data.neo4j.country.CountryRepository;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfigurationTests;
import org.springframework.boot.autoconfigure.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration}.
 *
 * @author Dave Syer
 * @author Oliver Gierke
 */
public class MixedNeo4jRepositoriesAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		this.context.close();
	}

	@Test
	public void testDefaultRepositoryConfiguration() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.datasource.initialize:false");
		this.context.register(TestConfiguration.class, BaseConfiguration.class);
		this.context.refresh();
		assertNotNull(this.context.getBean(CountryRepository.class));
	}

	@Test
	public void testMixedRepositoryConfiguration() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.datasource.initialize:false");
		this.context.register(MixedConfiguration.class, BaseConfiguration.class);
		this.context.refresh();
		assertNotNull(this.context.getBean(CountryRepository.class));
		assertNotNull(this.context.getBean(CityRepository.class));
	}

	@Test
	public void testJpaRepositoryConfigurationWithNeo4jTemplate() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.datasource.initialize:false");
		this.context.register(JpaConfiguration.class, BaseConfiguration.class);
		this.context.refresh();
		assertNotNull(this.context.getBean(CityRepository.class));
	}

	@Test
	@Ignore
	public void testJpaRepositoryConfigurationWithNeo4jOverlap() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.datasource.initialize:false");
		this.context.register(OverlapConfiguration.class, BaseConfiguration.class);
		this.context.refresh();
		assertNotNull(this.context.getBean(CityRepository.class));
	}

	@Test
	public void testJpaRepositoryConfigurationWithNeo4jOverlapDisabled() throws Exception {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.datasource.initialize:false",
				"spring.data.neo4j.repositories.enabled:false");
		this.context.register(OverlapConfiguration.class, BaseConfiguration.class);
		this.context.refresh();
		assertNotNull(this.context.getBean(CityRepository.class));
	}

	@Configuration
	@TestAutoConfigurationPackage(Neo4jAutoConfigurationTests.class)
	// Not this package or its parent
	@EnableNeo4jRepositories(basePackageClasses = Country.class)
	protected static class TestConfiguration {

	}

	@Configuration
	@TestAutoConfigurationPackage(Neo4jAutoConfigurationTests.class)
	@EnableNeo4jRepositories(basePackageClasses = Country.class)
	@EntityScan(basePackageClasses = City.class)
	@EnableJpaRepositories(basePackageClasses = CityRepository.class)
	protected static class MixedConfiguration {

	}

	@Configuration
	@TestAutoConfigurationPackage(Neo4jAutoConfigurationTests.class)
	@EntityScan(basePackageClasses = City.class)
	@EnableJpaRepositories(basePackageClasses = CityRepository.class)
	protected static class JpaConfiguration {

	}

	// In this one the Jpa repositories and the autoconfiguration packages overlap, so
	// Neo4j will try and configure the same repositories
	@Configuration
	@TestAutoConfigurationPackage(CityRepository.class)
	@EnableJpaRepositories(basePackageClasses = CityRepository.class)
	protected static class OverlapConfiguration {

	}

	@Configuration
	@Import(Registrar.class)
	protected static class BaseConfiguration {

	}

	protected static class Registrar implements ImportSelector {

		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			List<String> names = new ArrayList<String>();
			for (Class<?> type : new Class<?>[] { DataSourceAutoConfiguration.class,
					HibernateJpaAutoConfiguration.class,
					JpaRepositoriesAutoConfiguration.class,
					Neo4jDataAutoConfiguration.class,
					Neo4jRepositoriesAutoConfiguration.class }) {
				names.add(type.getName());
			}
			return names.toArray(new String[names.size()]);
		}
	}

}