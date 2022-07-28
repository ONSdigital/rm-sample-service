create sequence samplesummaryseq
    maxvalue 999999999999;

create sequence sampleunitseq
    maxvalue 999999999999;

create sequence collectionexercisejobseq
    maxvalue 999999999999;

create sequence messagelogseq
    maxvalue 999999999999;

create sequence reportpkseq
    maxvalue 999999999999;

create table sample_summary_state
(
    state_pk varchar(20) not null
        constraint samplesummarystate_pkey
            primary key
);

create table sample_summary
(
    sample_summary_pk bigint not null
        constraint samplesummary_pkey
            primary key,
    state_fk varchar(20) not null
        constraint statefk_fkey
            references sample_summary_state,
    ingest_date_time timestamp with time zone,
    id uuid not null
        constraint samplesummary_uuid_key
            unique,
    description varchar(250),
    total_sample_units integer,
    expected_collection_instruments integer,
    notes text,
    collection_exercise_id uuid,
    survey_id uuid,
    mark_for_deletion boolean default false
);

create index samplesummary_statefk_index
    on sample_summary (state_fk);

create table sample_unit_state
(
    state_pk varchar(20) not null
        constraint sampleunitstate_pkey
            primary key
);

create table sample_unit
(
    sample_unit_pk bigint not null
        constraint sampleunit_pkey
            primary key,
    sample_summary_fk bigint not null
        constraint samplesummary_fkey
            references sample_summary,
    sample_unit_ref varchar(20),
    sample_unit_type varchar(2),
    form_type varchar(10),
    state_fk varchar(20) not null
        constraint statefk_fkey
            references sample_unit_state,
    id uuid not null
        constraint sampleunit_uuid_key
            unique,
    party_id uuid,
    active_enrolment boolean default false,
    collection_instrument_id uuid,
    constraint sample_unit_sample_unit_ref_sample_summary_fk_key
        unique (sample_unit_ref, sample_summary_fk)
);

create index sampleunit_samplesummaryfk_index
    on sample_unit (sample_summary_fk);

create index sampleunit_statefk_index
    on sample_unit (state_fk);

create index sample_unit_sample_unit_ref_index
    on sample_unit (sample_unit_ref);