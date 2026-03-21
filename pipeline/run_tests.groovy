pipeline {{
    agent any

    stages {{

        stage('Run selected tests') {{
            steps {{
                script {{

                    def builds = [:]
                    def jobs = [:]

                    if (params.RUN_API) {{
                        jobs["API tests"] = {{
                            builds["api"] = build job: "api-tests", wait: true
                        }}
                    }}

                    if (params.RUN_UI) {{
                        jobs["UI tests"] = {{
                            builds["ui"] = build job: "ui-test", wait: true
                        }}
                    }}

                    if (params.RUN_MOBILE) {{
                        jobs["Mobile tests"] = {{
                            builds["mobile"] = build job: "mobile-tests", wait: true
                        }}
                    }}

                    if (jobs.isEmpty()) {{
                        error("No jobs selected")
                    }}

                    parallel jobs

                    env.API_BUILD = builds["api"]?.number
                    env.UI_BUILD = builds["ui"]?.number
                    env.MOBILE_BUILD = builds["mobile"]?.number
                }}
            }}
        }}

        stage('Collect Allure results') {{
            steps {{
                script {{

                    if (params.RUN_API) {{
                        copyArtifacts(
                                projectName: "api-tests",
                                selector: specific(env.API_BUILD),
                                filter: "target/allure-results/**",
                                target: "allure/api"
                        )
                    }}

                    if (params.RUN_UI) {{
                        copyArtifacts(
                                projectName: "ui-test",
                                selector: specific(env.UI_BUILD),
                                filter: "target/allure-results/**",
                                target: "allure/ui"
                        )
                    }}

                    if (params.RUN_MOBILE) {{
                        copyArtifacts(
                                projectName: "mobile-tests",
                                selector: specific(env.MOBILE_BUILD),
                                filter: "target/allure-results/**",
                                target: "allure/mobile"
                        )
                    }}

                }}
            }}
        }}

        stage('Merge results') {{
            steps {{
                sh '''
                mkdir -p merged-allure-results
                find allure -name "*.json" -exec cp {{}} merged-allure-results/ \\;
                '''
            }}
        }}

        stage('Allure report') {{
            steps {{
                allure([
                        results: [[path: 'merged-allure-results']]
                ])
            }}
        }}

    }}
}}