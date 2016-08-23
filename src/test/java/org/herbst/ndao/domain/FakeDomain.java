package org.herbst.ndao.domain;

import org.herbst.ndao.optimistic.DomainObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@DynamicUpdate
@Table(
        name = "fake_do"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FakeDomain extends DomainObject {

    private String uuid;

	public FakeDomain() {
	}

    @Column(name = "uuid")
    @NotNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
