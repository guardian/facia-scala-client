import type {GuStackProps} from '@guardian/cdk/lib/constructs/core';
import {GuStack} from '@guardian/cdk/lib/constructs/core';
import type {App} from 'aws-cdk-lib';
import { FederatedPrincipal} from "aws-cdk-lib/aws-iam";
import {GuGithubActionsRole} from "@guardian/cdk/lib/constructs/iam";
import {GuAllowPolicy} from "@guardian/cdk/lib/constructs/iam/policies/base-policy";

export class FaciaScalaClientTesting extends GuStack {
    constructor(scope: App, id: string, props: GuStackProps) {
        super(scope, id, props);
        let fapiBucketArn = "arn:aws:s3:::facia-tool-store"
        new GuGithubActionsRole(this, {
            assumedBy:  new FederatedPrincipal(
                `arn:aws:iam::${process.env.AWS_ACCOUNT_ID}:oidc-provider/token.actions.githubusercontent.com`,
                {
                    "StringEquals": { "token.actions.githubusercontent.com:aud": "sts.amazonaws.com" },
                    "StringLike": { "token.actions.githubusercontent.com:sub": "repo:guardian@164318/mobile-fastly-cache-purger@644326288:*"}
                },
                "sts:AssumeRoleWithWebIdentity"
            ),
            policies: [new GuAllowPolicy(
                this,
                "fapi-s3-bucket-access",
                {
                    actions: [
                        "s3:GetObject", // required by FAPI to download files
                        "s3:ListBucket", // avoiding S3 AccessDenied errors when FAPI tries to get nonexistent objects
                        "sts:AssumeRoleWithWebIdentity" // required for GitHub Actions to assume this role
                    ],
                    resources: [
                        `${fapiBucketArn}/DEV/*`, // object resource specified for s3:GetObject
                        fapiBucketArn // bucket resource specified for s3:ListBucket
                    ]
                }
            )],
            condition: {
                githubOrganisation: "guardian",
                repositories: "facia-scala-client:*",
                "StringEquals": {
                    "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
                },
                "StringLike": {
                    "token.actions.githubusercontent.com:sub": "repo:guardian@164318/facia-scala-client@20723151:*"
                }
            }
        })
    }
}
